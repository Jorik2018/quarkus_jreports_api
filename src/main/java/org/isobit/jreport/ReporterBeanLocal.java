package org.isobit.jreport;

import java.io.ByteArrayOutputStream;
import java.util.Map;
//import javax.ejb.Local;
import net.sf.jasperreports.engine.JasperReport;

//@Local
public interface ReporterBeanLocal {
  ByteArrayOutputStream export(JasperReport paramJasperReport, Map paramMap, String paramString);
  
  JasperReport getJasperReport(String paramString, int paramInt);
  
  void prepareReport(Map paramMap);
}


/* Location:              /Users/ealarcop/Documents/ib-report.jar!/org/isobit/jreport/ReporterBeanLocal.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */