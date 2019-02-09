package com.sppoti.htmltopdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.AbstractImageProvider;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j @Service public class HtmlToPdfConverterServiceImpl implements HtmlToPdfConverterService {
	
	private static final ClassLoader classLoader = FileUtils.class.getClassLoader();
	
	@Value("${pdf.storage.directory}") private String pdfStorageDirectory;
	
	@Value("${pdf.image.directory}") private String imageDirectory;
	
	@Value("${pdf.html.directory}") private String htmlDirectory;
	
	@Value("${pdf.css.directory}") private String cssDirectory;
	
	@Override public void convertAndSaveHtmlFile(String htmlFile, String cssFile) {
		
		try {
			InputStream resourceAsStream = classLoader.getResourceAsStream(htmlDirectory + htmlFile);
			byte[] bytes = IOUtils.toByteArray(resourceAsStream);
			byte[] convertedBytes = convertHtmlToPdf(bytes, cssFile, imageDirectory);
			saveFile(convertedBytes, pdfStorageDirectory, UUID.randomUUID().toString(), "pdf");
		} catch (IOException | DocumentException e) {
			log.error(e.getMessage(), e);
		}
		
	}
	
	private byte[] convertHtmlToPdf(final byte[] content, String css, final String imageDirectory)
			throws IOException, DocumentException
	{
		
		@Cleanup
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		final Document document = new Document(PageSize.A4);
		final PdfWriter writer = PdfWriter.getInstance(document, out);
		document.open();
		
		// CSS
		final CSSResolver cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(false);
		final InputStream cssStream = classLoader.getResourceAsStream(cssDirectory + css);
		final CssFile cssFile = XMLWorkerHelper.getCSS(cssStream);
		cssResolver.addCss(cssFile);
		
		// HTML
		final HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
		htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
		htmlContext.setImageProvider(new AbstractImageProvider() {
			@Override public String getImageRootPath() {
				return imageDirectory;
			}
		});
		
		// Pipelines
		final PdfWriterPipeline pdf = new PdfWriterPipeline(document, writer);
		final HtmlPipeline html = new HtmlPipeline(htmlContext, pdf);
		final CssResolverPipeline cssPipeline = new CssResolverPipeline(cssResolver, html);
		
		// XML Worker
		final XMLWorker worker = new XMLWorker(cssPipeline, true);
		final XMLParser p = new XMLParser(worker);
		p.parse(new ByteArrayInputStream(content), UTF_8);
		
		document.close();
		return out.toByteArray();
	}
	
	/**
	 * save the given document in the target directory.
	 *
	 * @param document
	 * 		document to save.
	 */
	private void saveFile(final byte[] document, final String outputDirectory, final String fileName,
						  final String extension)
	{
		//Save file to disk
		try {
			final String fullFileName;
			if (outputDirectory.endsWith("/")) {
				fullFileName = outputDirectory + fileName + "." + extension;
			} else {
				fullFileName = outputDirectory + "/" + fileName + "." + extension;
			}
			
			final FileOutputStream fos = new FileOutputStream(fullFileName);
			fos.write(document);
			fos.close();
		} catch (final IOException e) {
			log.error("Failed to save file: {} - with extension: {} - in folder: {}", fileName, outputDirectory,
					extension, e);
		}
	}
}
