package com.example.telegrambot.service.file;

import com.example.telegrambot.bot.FileDownloaderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class FileParserServiceImpl implements FileParserService {
    private final FileDownloaderService fileDownloaderService;

    public FileParserServiceImpl(@Lazy FileDownloaderService fileDownloaderService) {
        this.fileDownloaderService = fileDownloaderService;
    }

    @Override
    public String parse(Document document) throws IOException, TelegramApiException {
        InputStream inputStream = fileDownloaderService.downloadFile(document.getFileId());
        try (inputStream) {
            String mimeType = document.getMimeType();
            log.info("Parsing file '{}' with MIME type: {}", document.getFileName(), mimeType);
            if ("application/pdf".equals(mimeType)) {
                return parsePdf(inputStream);
            } else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType)) {
                return parseDocx(inputStream);
            } else {
                log.warn("Unsupported file type: {}", mimeType);
                return "Error: Unsupported file type.";
            }
        } catch (IOException e) {
            log.error("Failed to download or parse file", e);
            return "Error: Could not process the file.";
        }
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