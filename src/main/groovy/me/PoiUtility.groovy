package me;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
public class PoiUtility {

    public static CellReference getCellReference(book, name) {
        def refs = AreaReference.generateContiguous(null, book.getName(name).getRefersToFormula());
        return [refs[0].getFirstCell(),refs[0].getLastCell()];
    }
}
