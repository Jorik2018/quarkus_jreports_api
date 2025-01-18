package org.isobit.jreport;

import java.io.ByteArrayOutputStream;
import java.util.Map;
//import javax.ejb.EJB;
//import javax.ejb.Stateless;
import net.sf.jasperreports.engine.JasperReport;
//import org.isobit.app.ejb.SystemFacadeLocal;

//@Stateless
public class ReporterBean
  implements ReporterBeanLocal {
  //@EJB
  //private SystemFacadeLocal parametroFacade;
  
  public ByteArrayOutputStream export(JasperReport jasperReport, Map map, String FILE_EXTENSION) {
    try {
/* 18 */       return JR.export(JR.getJasperPrint(jasperReport, map, map), map, "pdf");
/* 19 */     } catch (Exception ex) {
/* 20 */       throw new RuntimeException("Error de ReporterBean.", ex);
    } 
  }

  
  public JasperReport getJasperReport(String url, int orientation) {
    try {
/* 27 */       return JR.getJasperReport(Object.class, url, 0);
/* 28 */     } catch (Exception ex) {
/* 29 */       throw new RuntimeException("Error de ReporterBean.", ex);
    } 
  }




  
  public void prepareReport(Map map) {
/* 38 */    // this.parametroFacade.prepareReport(map);
  }
}


/* Location:              /Users/ealarcop/Documents/ib-report.jar!/org/isobit/jreport/ReporterBean.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */