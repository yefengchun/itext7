package com.itextpdf.pdfa;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.util.MessageFormatUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.colorspace.PdfCieBasedCs;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class PdfATransparencyCheckTest extends ExtendedITextTest {
    public static final String sourceFolder = "./src/test/resources/com/itextpdf/pdfa/";
    public static final String cmpFolder = "./src/test/resources/com/itextpdf/pdfa/cmp/PdfATransparencyCheckTest/";
    public static final String destinationFolder = "./target/test/com/itextpdf/pdfa/PdfATransparencyCheckTest/";

    @BeforeClass
    public static void beforeClass() {
        createOrClearDestinationFolder(destinationFolder);
    }

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @Test
    public void textTransparencyNoOutputIntentTest() throws IOException {
        junitExpectedException.expect(PdfAConformanceException.class);
        junitExpectedException.expectMessage(MessageFormatUtil.format(PdfAConformanceException.THE_DOCUMENT_DOES_NOT_CONTAIN_A_PDFA_OUTPUTINTENT_BUT_PAGE_CONTAINS_TRANSPARENCY_AND_DOES_NOT_CONTAIN_BLENDING_COLOR_SPACE));

        PdfWriter writer = new PdfWriter(new java.io.ByteArrayOutputStream());
        PdfDocument pdfDocument = new PdfADocument(writer, PdfAConformanceLevel.PDF_A_3B, null);

        PdfFont font = PdfFontFactory.createFont(sourceFolder + "FreeSans.ttf", "Identity-H", true);

        PdfPage page1 = pdfDocument.addNewPage();

        PdfCanvas canvas = new PdfCanvas(page1);
        canvas.saveState();
        canvas.beginText()
                .moveText(36, 750)
                .setFontAndSize(font, 16)
                .showText("Page 1 without transparency")
                .endText()
                .restoreState();

        PdfPage page2 = pdfDocument.addNewPage();

        canvas = new PdfCanvas(page2);
        canvas.saveState();
        PdfExtGState state = new PdfExtGState();
        state.setFillOpacity(0.6f);
        canvas.setExtGState(state);
        canvas.beginText()
                .moveText(36, 750)
                .setFontAndSize(font, 16)
                .showText("Page 2 with transparency")
                .endText()
                .restoreState();

        pdfDocument.close();
    }

    @Test
    public void transparentTextWithGroupColorSpaceTest() throws IOException, InterruptedException {
        String outPdf = destinationFolder + "transparencyAndCS.pdf";
        String cmpPdf = cmpFolder + "cmp_transparencyAndCS.pdf";

        PdfDocument pdfDocument = new PdfADocument(new PdfWriter(outPdf), PdfAConformanceLevel.PDF_A_3B, null);
        PdfPage page = pdfDocument.addNewPage();
        PdfFont font = PdfFontFactory.createFont(sourceFolder + "FreeSans.ttf", "Identity-H", true);

        PdfCanvas canvas = new PdfCanvas(page);
        canvas.saveState();
        PdfExtGState state = new PdfExtGState();
        state.setFillOpacity(0.6f);
        canvas.setExtGState(state);
        canvas.beginText()
                .moveText(36, 750)
                .setFontAndSize(font, 16)
                .showText("Page 1 with transparency")
                .endText()
                .restoreState();

        PdfDictionary groupObj = new PdfDictionary();
        groupObj.put(PdfName.CS, new PdfCieBasedCs.CalGray(getCalGrayArray()).getPdfObject());
        groupObj.put(PdfName.Type, PdfName.Group);
        groupObj.put(PdfName.S, PdfName.Transparency);
        page.getPdfObject().put(PdfName.Group, groupObj);


        PdfPage page2 = pdfDocument.addNewPage();
        canvas = new PdfCanvas(page2);
        canvas.saveState();
        canvas.beginText()
                .moveText(36, 750)
                .setFontAndSize(font, 16)
                .showText("Page 2 without transparency")
                .endText()
                .restoreState();

        pdfDocument.close();
        compareResult(outPdf, cmpPdf);
    }

    @Test
    public void imageTransparencyTest() throws IOException {
        junitExpectedException.expect(PdfAConformanceException.class);
        junitExpectedException.expectMessage(MessageFormatUtil.format(PdfAConformanceException.THE_DOCUMENT_DOES_NOT_CONTAIN_A_PDFA_OUTPUTINTENT_BUT_PAGE_CONTAINS_TRANSPARENCY_AND_DOES_NOT_CONTAIN_BLENDING_COLOR_SPACE));

        PdfDocument pdfDoc = new PdfADocument(new PdfWriter(new java.io.ByteArrayOutputStream()), PdfAConformanceLevel.PDF_A_3B, null);

        PdfPage page = pdfDoc.addNewPage();
        PdfCanvas canvas = new PdfCanvas(page);

        page.getResources().setDefaultRgb(new PdfCieBasedCs.CalRgb(new float[]{0.3f, 0.4f, 0.5f}));

        canvas.saveState();
        canvas.addImage(ImageDataFactory.create(sourceFolder + "itext.png"), 0, 0, page.getPageSize().getWidth() / 2, false);
        canvas.restoreState();
        pdfDoc.close();
    }

    @Test
    public void nestedXObjectWithTransparencyTest() {
        junitExpectedException.expect(PdfAConformanceException.class);
        junitExpectedException.expectMessage(MessageFormatUtil.format(PdfAConformanceException.THE_DOCUMENT_DOES_NOT_CONTAIN_A_PDFA_OUTPUTINTENT_BUT_PAGE_CONTAINS_TRANSPARENCY_AND_DOES_NOT_CONTAIN_BLENDING_COLOR_SPACE));

        PdfWriter writer = new PdfWriter(new java.io.ByteArrayOutputStream());
        PdfDocument pdfDocument = new PdfADocument(writer, PdfAConformanceLevel.PDF_A_3B, null);
        PdfFormXObject form1 = new PdfFormXObject(new Rectangle(0, 0, 50, 50));
        PdfCanvas canvas1 = new PdfCanvas(form1, pdfDocument);
        canvas1.saveState();
        PdfExtGState state = new PdfExtGState();
        state.setFillOpacity(0.6f);
        canvas1.setExtGState(state);
        canvas1.circle(25, 25, 10);
        canvas1.fill();
        canvas1.restoreState();
        canvas1.release();
        form1.flush();

        //Create form XObject and flush to document.
        PdfFormXObject form = new PdfFormXObject(new Rectangle(0, 0, 50, 50));
        PdfCanvas canvas = new PdfCanvas(form, pdfDocument);
        canvas.rectangle(10, 10, 30, 30);
        canvas.stroke();
        canvas.addXObject(form1, 0, 0);
        canvas.release();
        form.flush();

        //Create page1 and add forms to the page.
        PdfPage page1 = pdfDocument.addNewPage();
        canvas = new PdfCanvas(page1);
        canvas.addXObject(form, 0, 0);
        canvas.release();

        pdfDocument.close();
    }

    private void compareResult(String outPdf, String cmpPdf) throws IOException, InterruptedException {
        String result = new CompareTool().compareByContent(outPdf, cmpPdf, destinationFolder, "diff_");
        if (result != null) {
            fail(result);
        }
    }

    private PdfArray getCalGrayArray() {
        PdfDictionary dictionary = new PdfDictionary();

        dictionary.put(PdfName.Gamma, new PdfNumber(2.2));

        PdfArray whitePointArray = new PdfArray();
        whitePointArray.add(new PdfNumber(0.9505));
        whitePointArray.add(new PdfNumber(1.0));
        whitePointArray.add(new PdfNumber(1.089));
        dictionary.put(PdfName.WhitePoint, whitePointArray);

        PdfArray array = new PdfArray();
        array.add(PdfName.CalGray);
        array.add(dictionary);

        return array;
    }
}
