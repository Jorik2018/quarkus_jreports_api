package org.isobit.jreport;

import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
//import org.isobit.swing.util.dTableContent;
import org.isobit.util.SimpleStore;
import javax.sql.DataSource;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.table.TableModel;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.StreamingOutput;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import org.isobit.app.X;
import org.isobit.app.action.PreviewAction;
import org.isobit.data.Store;
import org.isobit.util.XFile;
import org.isobit.swing.JSViewer;
import org.isobit.swing.XAction;
import org.isobit.swing.XView;
import org.isobit.util.XUtil;

public class JR
    extends XAction
    implements PreviewAction.ReportEngine {
  public static final String IS_ONE_PAGE_PER_SHEET = "IS_ONE_PAGE_PER_SHEET";

  public static Object open(ByteArrayOutputStream out) {
    try {
      File file = File.createTempFile("preview", ".pdf");
      System.out.println("open.file:///" + file);
      if ((new File("")).getCanonicalFile().getParentFile().getParentFile().getName()
          .equals(X.PROJECTS_PATH)) {
        FileOutputStream fos = new FileOutputStream(XFile.getFile(file));
        out.writeTo(fos);
        out.close();
        try {
          Desktop.getDesktop().open(file);
          X.log("Desktop.getDesktop().open().file=" + file);
        } catch (HeadlessException e) {
          Runtime.getRuntime().exec("\"C:\\Program Files\\Nitro\\Pro 9\\NitroPDF.exe\" \"" + file + "\"");
        }
        return null;
      }
      return null;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static class Model {
    SimpleStore sFuente = new SimpleStore(new ArrayList(),
        new String[] { "id_fuente", "tipo", "codigo", "fuente_verificacion", "estado", "fecha_reg", "delta" });

    SimpleStore sDetalle = new SimpleStore(new ArrayList(), new String[] { "tipo", "id_fuepar",
        "id_autoevaluacion", "id_fuente", "titulo", "direccion", "peso", "pagina", "id_estado", "orientation" });

    private Map map;

    public Model() {
      this(new HashMap<>());
    }

    public Model(Map m) {
      String ss = X.toText(m.get("#REPORT_PATH"));
      this.map = m;
      this.map.put(DataSource.class, this.sFuente);
      this.map.put("REPORT_DETAIL", this.sDetalle);
      this.map.put("SUBREPORT_DIR", ss + "/org/isobit/jreport/reportDetail.jasper");
      this.map.put("TEMPLATE_H", ss + "/org/isobit/jreport/reportGenericH.jasper");
    }

    public Model setProperties(Object[] p) {
      this.sFuente.getData().add(p);
      return this;
    }

    public Model add(Object[] o) {
      this.sDetalle.getData().add(o);
      return this;
    }

    public Model put(Object k, Object v) {
      this.map.put(k, v);
      return this;
    }

    public Map getConfig() {
      return this.map;
    }
  }

  public static int install() {
    PreviewAction.reportEngine.put("jasper", new JR());
    return 0;
  }

  public static String EXTENSION = "-EXTENSION";
  private static Boolean DEVELOPING;
  private static String UPLOAD_DIR;

  public static Object open(Model m) {
    return open("/org/isobit/jreport/reportGenericV.jasper", m);
  }

  public static Object open(String template, Model m) {
    return open(template, m.getConfig());
  }

  public static String getPath(String jasperFileName) throws IOException {
    File f = (new File("")).getCanonicalFile();
    if (f.getParentFile().getName().equals(X.PROJECTS_PATH)) {
      jasperFileName = jasperFileName.replace("/", "\\");
      File f2 = new File(f, "src\\java" + jasperFileName);
      if (!f2.exists()) {
        f2 = new File(f, "src" + jasperFileName);
      }
      if (!f2.exists()) {
        f2 = new File(f.getParent(),
            "/" + jasperFileName.replace("\\", "/").split("/")[3] + "/src" + jasperFileName);
      }
      return f2.getCanonicalPath();
    }
    X.log("=>" + jasperFileName);
    return jasperFileName;
  }

  public static Object open(JasperPrint jasperPrint, Object o) throws JRException {
    try {
      Map m = o instanceof Map ? (Map) o : new HashMap();
      if (o instanceof List) {
        m.put(DataSource.class, o);
      }
      m.put("APP_NAME", "Sistema de Información Integral de Gestión Administrativa y Académica - SIIGAA");
      String extension = (String) XUtil.isEmpty(m.remove(EXTENSION), "pdf");
      File file = File.createTempFile("preview", "." + extension);

      try (FileOutputStream fos = new FileOutputStream(file)) {
        fos.write(
            export(jasperPrint, m, extension)
                .toByteArray());
      }

      System.out.println("Opening '" + file + "'");

      Desktop.getDesktop().open(file);

    } catch (IOException e) {
      alert(e, o);
      throw new RuntimeException(e);
    }
    return null;
  }

  public static Object open(String jasperFileName, Object o) {
    return open((jasperFileName != null) ? getJasperReport(Object.class, jasperFileName, 0) : null, o);
  }

  public static Object open(JasperReport jasperReport, Object o) {
    try {
      Map map = o instanceof Map ? (Map) o : new HashMap();
      if (o instanceof List) {
        map.put(DataSource.class, o);
      }
      map.put("APP_NAME", "Sistema de Información Integral de Gestión Administrativa y Académica - SIIGAA");
      map.put("SUBREPORT_DIR", getUploadDir());
      return open(getJasperPrint(jasperReport, map.get(DataSource.class), map), map);
    } catch (IOException | JRException e) {
      e.printStackTrace();
      alert(e, o);
      throw new RuntimeException(e);
    }
  }

  public static StreamingOutput open(JasperPrint masterJP, Map map) {
    try {
      String extension = (String) XUtil.isEmpty(map.remove(EXTENSION), "pdf");

      byte[] a = export(masterJP, map, extension).toByteArray();
      return output -> {
        try {
          output.write(a);
          output.flush();
        } catch (IOException e) {
          throw new WebApplicationException("File Not Found !!");
        }
      };
    } catch (IOException | JRException e) {
      e.printStackTrace();
      alert(e, map);
      throw new RuntimeException(e);
    }
  }

  public static Object alert(Exception e, Object params) {
    try {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      File file = File.createTempFile("preview", ".pdf");
      String fileName = "/org/isobit/jreport/jr/error.jasper";
      File f = (new File("")).getCanonicalFile();
      if (f.getParentFile().getName().equals(X.PROJECTS_PATH)) {
        fileName = (new File(f, "src\\java" + fileName.replace("/", "\\"))).getAbsolutePath();

      } else {
        return null;
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return null;
  }

  public static ByteArrayOutputStream export(JasperPrint masterJP, Map map, String FILE_EXTENSION)
      throws IOException, JRException {
    TableModel tm = null;
    JRExporter exp = null;
    if ("rtf".equals(FILE_EXTENSION)) {
      exp = new JRRtfExporter();
    } else if ("docx".equals(FILE_EXTENSION)) {
      exp = new JRDocxExporter();

    } else if ("xls".equals(FILE_EXTENSION)) {

      exp = new JRXlsExporter();
      exp.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET,
          Boolean.valueOf(XUtil.booleanValue(map.get("IS_ONE_PAGE_PER_SHEET"))));
      exp.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
      exp.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
      exp
          .setParameter((JRExporterParameter) JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
    } else if ("pdf".equals(FILE_EXTENSION)) {
      exp = new JRPdfExporter();
    } else {
      exp.setParameter(JRHtmlExporterParameter.IMAGES_URI, "image?image=");
    }

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    exp.setParameter(JRExporterParameter.OUTPUT_STREAM, output);
    if (map.containsKey("xyz")) {
      exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, map.remove("xyz"));
    } else {
      exp.setParameter(JRExporterParameter.JASPER_PRINT, masterJP);
    }
    exp.exportReport();
    return output;
  }

  public static void setUPLOAD_DIR(String UPLOAD_DIR) {
    JR.UPLOAD_DIR = UPLOAD_DIR;
  }

  private static String getUploadDir() {
    if (UPLOAD_DIR == null) {

      UPLOAD_DIR = (String) ClientBuilder.newClient()
          .target("http://localhost:" + X.getRequest().getLocalPort() + "/api/system/upload-dir").request()
          .get(String.class) + "/jasper";
    }
    return UPLOAD_DIR;
  }

  public static JasperReport getJasperReport(Class cls, String jasperFileName, int orientation) {
    jasperFileName = (jasperFileName != null) ? jasperFileName
        : (cls.getCanonicalName().replace(".", "/") + ((orientation == 1) ? "_h" : "") + ".jasper");
    File f = new File(getUploadDir() + jasperFileName);
    System.out.println(f);
    try {
      if (f.exists()) {
        return (JasperReport) JRLoader.loadObject(f);
      }

      File f1 = (new File("")).getCanonicalFile();
      InputStream is = null;
      if (f1.exists() && f1.isFile()) {
        is = new FileInputStream(f1);
      } else {
        is = X.class.getClassLoader().getResourceAsStream(jasperFileName);
      }
      if (is == null) {
        is = X.class.getClassLoader().getResourceAsStream(
            jasperFileName = cls.getPackage().getName().replace(".", "/") + "/" + jasperFileName);
      }

      X.log("is=" + is);
      return (JasperReport) JRLoader.loadObject(is);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public Object createView(Object w) throws JRException {
    JPanel p = (JPanel) w;
    JSViewer j = new JSViewer(p);
    Store s = (Store) p.getClientProperty(TableModel.class);
    s.setDefault("#editor", Boolean.valueOf(false));
    j.setStore(s);
    j.putClientProperty("fileName", p.getClientProperty("fileName"));
    j.putClientProperty(Container.class, w);
    j.setBackground(Color.red);
    return j;
  }

  public void preview(Object report) {
  }

  public static JasperPrint getJasperPrint(Object view) throws JRException {
    InputStream is;
    JComponent viewCmp = null;
    Map map = null;
    Object tm = null;
    String jasperFile = null;
    if (view instanceof JComponent) {
      viewCmp = (JComponent) view;
      JComponent parent = (JComponent) viewCmp.getClientProperty(Container.class);
      if (parent != null) {
        viewCmp = parent;
      }
      map = (Map) viewCmp.getClientProperty("#params");
      tm = viewCmp.getClientProperty(TableModel.class);
      jasperFile = (String) viewCmp.getClientProperty("fileName");
    }
    if (map == null && view instanceof XView) {
      Store s = ((XView) view).getStore();
      map = (Map) s.getDefault("#params");
      tm = s;
    }

    if ((jasperFile == null || !jasperFile.endsWith(" ")) && tm != null) {
      Class<?> c = (tm instanceof Store) ? ((Store) tm).getQ().getClass() : tm.getClass();
      while (c != null && c.isAnonymousClass()) {
        c = c.getSuperclass();
      }
      if (jasperFile == null) {
        jasperFile = c.getCanonicalName().replace(".", "/") + ".jasper";
      }

      is = X.class.getClassLoader().getResourceAsStream(jasperFile);
      if (is == null) {
        is = X.class.getClassLoader()
            .getResourceAsStream(jasperFile = c.getPackage().getName().replace(".", "/") + "/" + jasperFile);
      }

      viewCmp.putClientProperty("fileName", jasperFile + " ");
    } else {
      is = X.class.getClassLoader().getResourceAsStream(jasperFile.trim());
      if (is == null) {
        is = X.class.getClassLoader().getResourceAsStream(jasperFile.trim());
      }
    }
    JasperReport jr = (JasperReport) JRLoader.loadObject(is);
    try {
      return getJasperPrint(jr, tm, (map != null) ? map : new HashMap<>());
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public void print(Object report) {
    try {
      JasperPrintManager.printReport(getJasperPrint(report), true);
    } catch (Exception e) {
      X.alert(e);
    }
  }

  public static JRDataSource reset(JRDataSource ds) throws JRException {
    if (ds instanceof JRRewindableDataSource) {
      ((JRRewindableDataSource) ds).moveFirst();
    }
    return ds;
  }

  public static void printQuitely(JasperPrint jasperPrint) throws JRException {
    try {
      JasperPrintManager.printReport(jasperPrint, false);
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  public static JasperPrint getJasperPrint(JasperReport jasperReport, Object ds, Map map)
      throws JRException, IOException {
    JasperPrint masterJP = null;
    System.out.println("=========================DADADADAD");
    if (map != null) {
      if (map.containsKey("REPORT_OFFSET")) {
        map.put("REPORT_OFFSET", Integer.valueOf(XUtil.intValue(map.get("REPORT_OFFSET"))));
      }

      TableModel tm = (TableModel) map.get("REPORT_DETAIL");
      String DO_NOT_PROCESS = X.toText(map.get("DO_NOT_PROCESS"));
      Map<Object, Object> extras = new HashMap<>();

      for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
        Object v = entry.getValue();
        Object k = entry.getKey();
        Object type = v;
        if (DO_NOT_PROCESS.contains(k.toString())) {
          continue;
        }
        if (v instanceof Object[]) {
          if (((Object[]) v).length > 1) {
            type = ((Object[]) v)[0];
            v = ((Object[]) v)[1];
          }
        } else if (v instanceof TableModel || v instanceof ResultSet) {
          type = DataSource.class;
        }
        if (type == DataSource.class) {
          if (v instanceof TableModel) {
            map.put(entry.getKey(), v = new JRTableModelDataSource((TableModel) v));
          } else if (v instanceof Collection) {
            map.put(entry.getKey(), v = new JRBeanCollectionDataSource((Collection) v));
          } else if (v instanceof Object[]) {
            map.put(entry.getKey(), v = new JRBeanArrayDataSource((Object[]) v));
          } else if (v instanceof ResultSet) {
            map.put(entry.getKey(), v = new JRResultSetDataSource((ResultSet) v));
          }
        } else if (v instanceof Collection) {
          map.put(k, v = new JRBeanCollectionDataSource((Collection) v));
        } else if (v instanceof Object[]) {
          map.put(k, v = new JRBeanArrayDataSource((Object[]) v));
        } else if (v instanceof TableModel) {
          map.put(entry.getKey(), v = new JRTableModelDataSource((TableModel) v));
        } else if (v instanceof File) {
          JsonDataSource json = new JsonDataSource((File) v);
          json.setLocale(Locale.US);
          json.setDatePattern("yyyy-MM-dd");
          extras.put("net.sf.jasperreports.json.date.pattern", "yyyy-MM-dd");
          extras.put("REPORT_LOCALE", Locale.US);
          map.put(k, json);
        } else if (v instanceof JRRewindableDataSource) {
          ((JRRewindableDataSource) v).moveFirst();
        } else if (v instanceof ImageIcon) {
          map.put(entry.getKey(), ((ImageIcon) v).getImage());
        }
      }
      map.putAll(extras);

      JRDataSource reportDS = null;
      if (ds instanceof JRDataSource) {
        reportDS = (JRDataSource) ds;
      } else if (ds instanceof TableModel) {
        reportDS = new net.sf.jasperreports.engine.data.JRTableModelDataSource((TableModel) ds);
      } else if (ds instanceof Collection) {
        reportDS = new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource((Collection) ds);
      } else if (ds instanceof File) {
        JsonDataSource json = new JsonDataSource((File) ds);
        json.setLocale(Locale.US);
        reportDS = json;
      }
      if (map.containsKey("TOC")) {
        // map.put("TOC", new dTableContent());
      }
      int offset = 0;
      int tocPosition = 0;

      if (tm instanceof SimpleStore) {
        Object currentOrientation = null;
        SimpleStore detailStore = (SimpleStore) tm;
        JRDataSource reportDetailDS = (JRDataSource) map.get("REPORT_DETAIL");
        List<Object[]> dataDetailStore = detailStore.getData();
        Object lastOrientation = Boolean.valueOf(false);
        int k = 0;
        int rowCount = tm.getRowCount();
        List<JasperPrint> jaspertPrintList = new ArrayList();

        JasperReport[] jr2 = { jasperReport, getJasperReport(null, "" + map.get("TEMPLATE_H"), 1) };

        if (jasperReport == null) {
          for (int i = 0; i < rowCount; i++) {
            reportDS = (JRDataSource) map.get("R" + (i + 1));
            String reportFileName = ((Object[]) dataDetailStore.get(i))[5].toString();
            JasperReport jr = getJasperReport(null, reportFileName, 0);
            JasperPrint jp = reportDS != null
                ? JasperFillManager.fillReport(jr, map, reportDS)
                : JasperFillManager.fillReport(jr, map, (Connection) map.get("cnx"));
            offset += jp.getPages().size();
            map.put("REPORT_OFFSET", offset);
            jaspertPrintList.add(jp);
          }
        } else {

          jr2 = new JasperReport[] { jasperReport, getJasperReport(null, "" + map.get("TEMPLATE_H"), 1) };

          for (int i = 0; i <= rowCount; i++) {
            if (i < rowCount) {
              currentOrientation = ((Object[]) dataDetailStore.get(i))[9];
              Object[] o = dataDetailStore.get(i);
              if (o.length > 10) {
                map.putAll((Map<? extends String, ? extends Integer>) o[10]);
              }
            }
            k++;
            if (i == rowCount || !lastOrientation.equals(currentOrientation)) {
              if (lastOrientation != Boolean.FALSE) {
                JasperPrint jp;
                detailStore.setStart(i - k);
                detailStore.setSize(k);
                reset(reportDS);
                reset(reportDetailDS);

                if (lastOrientation instanceof String) {
                  tocPosition++;
                  int ii = XUtil.intValue(((Object[]) dataDetailStore.get(i - 1))[1]);
                  jp = JasperFillManager.fillReport(
                      getJasperReport(null, (String) lastOrientation, 0), map,
                      (ii == 0) ? reportDS
                          : (JRDataSource) map
                              .get("R" + ii));

                } else {

                  jp = (reportDS != null)
                      ? JasperFillManager.fillReport(jr2[XUtil.intValue(lastOrientation)], map, reportDS)
                      : JasperFillManager.fillReport(jr2[XUtil.intValue(lastOrientation)], map,
                          (Connection) map.get("cnx"));
                  offset += jp.getPages().size();
                  map.put("REPORT_OFFSET", Integer.valueOf(offset));
                }
                jaspertPrintList.add(jp);
              }
              k = 0;
              lastOrientation = currentOrientation;
            }
          }
        }

        if (map.containsKey("TOC")) {
          TableModel tm2 = (TableModel) map.get("TOC");
          map.put("R1", new JRTableModelDataSource(tm2));
          detailStore.setStart(0);
          detailStore.setSize(1);
          detailStore.getData().clear();
          detailStore.getData()
              .add(new Object[] { "P", Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(0), null,
                  "/org/isobit/jreport/table_content.jasper",
                  Integer.valueOf(0), Integer.valueOf(10), Integer.valueOf(0), Integer.valueOf(1) });
          reset(reportDS);
          reset(reportDetailDS);
          map.put("REPORT_OFFSET", Integer.valueOf(0));
          jaspertPrintList.add(tocPosition, JasperFillManager.fillReport(jr2[0], map, reportDS));
        }
        map.put("xyz", jaspertPrintList);
      } else {
        masterJP = (reportDS != null
            ? JasperFillManager.fillReport(jasperReport, map, reportDS)
            : JasperFillManager.fillReport(jasperReport, map, (Connection) map.get("cnx")));
      }
    }
    return masterJP;
  }
}
