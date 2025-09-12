package com.example.telegrambot.infra.file;

import com.example.telegrambot.infra.telegram.FileDownloaderService;
import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class FileParserServiceImpl implements FileParserService {
    private static final String PDF_MIME_TYPE = "application/pdf";
    private static final String DOCX_MIME_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final FileDownloaderService fileDownloaderService;

    public FileParserServiceImpl(FileDownloaderService fileDownloaderService) {
        this.fileDownloaderService = fileDownloaderService;
    }

    @Override
    public String parse(Document document) throws IOException, TelegramApiException {
        String baseMimeType = getBaseMimeType(document.getMimeType());
        log.info("Received file '{}' with base MIME type: {}",
                document.getFileName(), baseMimeType);

        if (PDF_MIME_TYPE.equals(baseMimeType)) {
            return processFile(document, this::parsePdf);
        } else if (DOCX_MIME_TYPE.equals(baseMimeType)) {
            return processFile(document, this::parseDocx);
        } else {
            log.warn("Unsupported file type: {}", document.getMimeType());
            return "Error: Unsupported file type. Please send a .pdf or .docx file.";
        }
    }

    private String getBaseMimeType(String rawMimeType) {
        if (rawMimeType == null) {
            return "";
        }
        try {
            return new MimeType(rawMimeType).getBaseType();
        } catch (MimeTypeParseException e) {
            log.warn("Could not parse MIME type: '{}'", rawMimeType);
            return "";
        }
    }

    private String processFile(Document document, FileContentParser parser)
            throws IOException, TelegramApiException {
        try (InputStream inputStream = fileDownloaderService.downloadFile(document.getFileId())) {
            return parser.parse(inputStream);
        }
    }

    @FunctionalInterface
    private interface FileContentParser {
        String parse(InputStream inputStream) throws IOException;
    }

    private String parseDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    private String parsePdf(InputStream inputStream) throws IOException {
        byte[] fileBytes = inputStream.readAllBytes();

        try (PDDocument document = Loader.loadPDF(fileBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text;
        }
    }
}
