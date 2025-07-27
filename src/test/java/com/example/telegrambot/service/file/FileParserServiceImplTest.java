package com.example.telegrambot.service.file;

import com.example.telegrambot.bot.FileDownloaderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileParserServiceImplTest {
    @Mock
    private FileDownloaderService fileDownloaderService;

    @InjectMocks
    private FileParserServiceImpl fileParserService;

    @Test
    void parse_withValidPdf_shouldReturnCorrectText() throws IOException, TelegramApiException {
        Document document = createTestDocument("pdf_id", "cv.pdf", "application/pdf");
        InputStream pdfInputStream = getResourceFileStream("test_cv.pdf");
        when(fileDownloaderService.downloadFile("pdf_id")).thenReturn(pdfInputStream);

        String actualText = fileParserService.parse(document);

        assertTrue(actualText.contains("fintech platform"));
        verify(fileDownloaderService).downloadFile("pdf_id");
    }

    @Test
    void parse_withValidDocx_shouldReturnCorrectText() throws IOException, TelegramApiException {
        Document document = createTestDocument("docx_id", "cv.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        InputStream docxInputStream = getResourceFileStream("test_cv.docx");
        when(fileDownloaderService.downloadFile("docx_id")).thenReturn(docxInputStream);

        String actualText = fileParserService.parse(document);

        assertTrue(actualText.contains("fintech platform"));
        verify(fileDownloaderService).downloadFile("docx_id");
    }

    @Test
    void parse_withUnsupportedFileType_shouldReturnErrorMessage() throws IOException, TelegramApiException {
        Document document = createTestDocument("img_id", "photo.jpg", "image/jpeg");

        String result = fileParserService.parse(document);

        assertEquals("Error: Unsupported file type. Please send a .pdf or .docx file.", result);
        verifyNoInteractions(fileDownloaderService);
    }

    @Test
    void parse_whenDownloaderThrowsException_shouldPropagateException() throws IOException, TelegramApiException {
        Document document = createTestDocument("fail_id", "cv.pdf", "application/pdf");
        when(fileDownloaderService.downloadFile("fail_id")).thenThrow(new TelegramApiException("Download failed"));

        assertThrows(TelegramApiException.class, () -> fileParserService.parse(document));
    }

    @Test
    void parse_withNullMimeType_shouldBeHandledGracefully() throws IOException, TelegramApiException {
        Document document = createTestDocument("null_mime_id", "file.tmp", null);

        String result = fileParserService.parse(document);

        assertEquals("Error: Unsupported file type. Please send a .pdf or .docx file.", result);
        verifyNoInteractions(fileDownloaderService);
    }

    private Document createTestDocument(String fileId, String fileName, String mimeType) {
        Document document = new Document();
        document.setFileId(fileId);
        document.setFileName(fileName);
        document.setMimeType(mimeType);
        return document;
    }

    private InputStream getResourceFileStream(String fileName) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
        assertNotNull(stream, "Test file " + fileName + " not found in resources");
        return stream;
    }
}