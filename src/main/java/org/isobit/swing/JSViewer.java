package org.isobit.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.TableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRImageMapRenderer;
import net.sf.jasperreports.engine.JRPrintAnchorIndex;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintFrame;
import net.sf.jasperreports.engine.JRPrintHyperlink;
import net.sf.jasperreports.engine.JRPrintImageAreaHyperlink;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRRenderable;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.export.JRGraphics2DExporter;
import net.sf.jasperreports.engine.export.JRGraphics2DExporterParameter;
import net.sf.jasperreports.engine.print.JRPrinterAWT;
import net.sf.jasperreports.engine.type.HyperlinkTypeEnum;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.SimpleFileResolver;
import net.sf.jasperreports.engine.xml.JRPrintXmlLoader;
import net.sf.jasperreports.view.JRHyperlinkListener;
import net.sf.jasperreports.view.JRSaveContributor;
import net.sf.jasperreports.engine.JRPrintImage;
import org.isobit.app.X;
import org.isobit.app.action.*;
import org.isobit.data.Store;
import org.isobit.jreport.JR;
import net.sf.jasperreports.engine.util.JRClassLoader;
import net.sf.jasperreports.engine.util.JRLoader;
import org.isobit.util.XMap;
import java.awt.image.BufferedImage;

public final class JSViewer extends javax.swing.JPanel implements JRHyperlinkListener,XView{
  
  private JasperPrint jasperPrint;
  private Store store;
  private static final long serialVersionUID = 10200L;
  public static final String VIEWER_RENDER_BUFFER_MAX_SIZE = "100000";
  protected static final int TYPE_FILE_NAME = 1;
  protected static final int TYPE_INPUT_STREAM = 2;
  protected static final int TYPE_OBJECT = 3;
  public static final int REPORT_RESOLUTION = 72;

  public JSViewer() {
    this((JasperPrint) null);
  }

  public JSViewer(Store store) {
    this();
    this.store = store;
    putClientProperty("fileName", store.getDefault("#template"));
    putClientProperty(Package.class, store.getClass());
    putClientProperty(TableModel.class, store);
  }

  public JSViewer(JasperPrint jrPrint) {
    this(jrPrint, (Locale) null);
  }

  public JSViewer(Component target) throws JRException {
    this(JR.getJasperPrint(target));
  }

  public void actionPerformed(String nameChild) {
    JButton b = (JButton) getComponent(nameChild);
    b.getActionListeners()[0].actionPerformed(new ActionEvent(b, 0, nameChild));
  }

  public Component getComponent(String childName) {
    return null;
  }

  public String getMessage() {
    return "";
  }

  protected final float MIN_ZOOM = 0.5F;
  protected final float MAX_ZOOM = 10.0F;
  protected int[] zooms = new int[] { 50, 75, 100, 125, 150, 175, 200, 250, 400, 800 };
  protected int defaultZoomIndex = 2;

  protected int type = 1;

  protected boolean isXML;

  protected String reportFileName;

  protected SimpleFileResolver fileResolver;

  private int pageIndex;

  private boolean pageError;

  protected float zoom;
  private JRGraphics2DExporter exporter;
  private int screenResolution = 72;

  protected float realZoom;

  private DecimalFormat zoomDecimalFormat = new DecimalFormat("#.##");
  private ResourceBundle resourceBundle;
  private int downX;
  private int downY;
  private List hyperlinkListeners = new ArrayList();
  private Map linksMap = new HashMap<>();

  private MouseListener mouseListener = new MouseAdapter() {
    public void mouseClicked(MouseEvent evt) {
      JSViewer.this.hyperlinkClicked(evt);
    }
  };

  protected KeyListener keyNavigationListener = new KeyListener() {
    public void keyTyped(KeyEvent evt) {
    }

    public void keyPressed(KeyEvent evt) {
      JSViewer.this.keyNavigate(evt);
    }

    public void keyReleased(KeyEvent evt) {
    }
  };

  protected List saveContributors = new ArrayList();
  protected File lastFolder;
  protected JRSaveContributor lastSaveContributor;
  protected JToggleButton btnActualSize;
  protected JButton btnFirst;
  protected JToggleButton btnFitPage;
  protected JToggleButton btnFitWidth;
  protected JButton btnLast;
  protected JButton btnNext;
  protected JButton btnPrevious;
  protected JButton btnPrint;
  protected JButton btnReload;

  public JSViewer(String fileName, boolean isXML) throws JRException {
    this(fileName, isXML, (Locale) null);
  }

  protected JButton btnZoomIn;
  protected JButton btnZoomOut;
  protected JComboBox cmbZoom;
  private PageRenderer lblPage;
  protected JLabel lblStatus;
  private JPanel pnlInScroll;
  private JPanel pnlLinks;
  private JPanel pnlMain;
  private JPanel pnlPage;
  private JScrollPane scrollPane;
  protected JPanel tlbToolBar;
  protected JTextField txtGoTo;

  public JSViewer(InputStream is, boolean isXML) throws JRException {
    this(is, isXML, (Locale) null);
  }

  public JSViewer(String fileName, boolean isXML, Locale locale) throws JRException {
    this(fileName, isXML, locale, (ResourceBundle) null);
  }

  public JSViewer(InputStream is, boolean isXML, Locale locale) throws JRException {
    this(is, isXML, locale, (ResourceBundle) null);
  }

  public JSViewer(JasperPrint jrPrint, Locale locale) {
    this(jrPrint, locale, (ResourceBundle) null);
  }

  public JSViewer(String fileName, boolean isXML, Locale locale, ResourceBundle resBundle) throws JRException {
    initResources(locale, resBundle);
    setScreenDetails();
    setZooms();
    initComponents();
    loadReport(fileName, isXML);
    this.cmbZoom.setSelectedIndex(this.defaultZoomIndex);
    initSaveContributors();
    addHyperlinkListener(this);
  }

  public JSViewer(InputStream is, boolean isXML, Locale locale, ResourceBundle resBundle) throws JRException {
    initResources(locale, resBundle);
    setScreenDetails();
    setZooms();
    initComponents();
    loadReport(is, isXML);
    this.cmbZoom.setSelectedIndex(this.defaultZoomIndex);
    initSaveContributors();
    addHyperlinkListener(this);
  }

  public JSViewer(JasperPrint jrPrint, Locale locale, ResourceBundle resBundle) {
    initResources(locale, resBundle);
    setScreenDetails();
    setZooms();
    initComponents();
    if (jrPrint != null)
      loadReport(jrPrint);
    this.cmbZoom.setSelectedIndex(this.defaultZoomIndex);
    initSaveContributors();
    addHyperlinkListener(this);
  }

  private void setScreenDetails() {
    // this.screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
  }

  public XView clear() {
    this.store.clear();
    emptyContainer(this);
    this.jasperPrint = null;
    return this;
  }

  protected void setZooms() {
  }

  public void addSaveContributor(JRSaveContributor contributor) {
    this.saveContributors.add(contributor);
  }

  public void removeSaveContributor(JRSaveContributor contributor) {
    this.saveContributors.remove(contributor);
  }

  public JRSaveContributor[] getSaveContributors() {
    return (JRSaveContributor[]) this.saveContributors
        .toArray((Object[]) new JRSaveContributor[this.saveContributors.size()]);
  }

  public void setSaveContributors(JRSaveContributor[] saveContribs) {
    this.saveContributors = new ArrayList();
    if (saveContribs != null)
      this.saveContributors.addAll(Arrays.asList(saveContribs));
  }

  public void addHyperlinkListener(JRHyperlinkListener listener) {
    this.hyperlinkListeners.add(listener);
  }

  public void removeHyperlinkListener(JRHyperlinkListener listener) {
    this.hyperlinkListeners.remove(listener);
  }

  public JRHyperlinkListener[] getHyperlinkListeners() {
    return (JRHyperlinkListener[]) this.hyperlinkListeners
        .toArray((Object[]) new JRHyperlinkListener[this.hyperlinkListeners.size()]);
  }

  protected void initResources(Locale locale, ResourceBundle resBundle) {
    if (locale != null) {
      setLocale(locale);
    } else {
      setLocale(Locale.getDefault());
    }
    if (resBundle == null) {
      this.resourceBundle = ResourceBundle.getBundle("net/sf/jasperreports/view/viewer", getLocale());
    } else {
      this.resourceBundle = resBundle;
    }
  }

  protected String getBundleString(String key) {
    return this.resourceBundle.getString(key);
  }

  protected void initSaveContributors() {
    String[] DEFAULT_CONTRIBUTORS = { "net.sf.jasperreports.view.save.JRPrintSaveContributor",
        "net.sf.jasperreports.view.save.JRPdfSaveContributor", "net.sf.jasperreports.view.save.JRRtfSaveContributor",
        "net.sf.jasperreports.view.save.JROdtSaveContributor", "net.sf.jasperreports.view.save.JRDocxSaveContributor",
        "net.sf.jasperreports.view.save.JRHtmlSaveContributor",
        "net.sf.jasperreports.view.save.JRSingleSheetXlsSaveContributor",
        "net.sf.jasperreports.view.save.JRMultipleSheetsXlsSaveContributor",
        "net.sf.jasperreports.view.save.JRCsvSaveContributor", "net.sf.jasperreports.view.save.JRXmlSaveContributor",
        "net.sf.jasperreports.view.save.JREmbeddedImagesXmlSaveContributor" };

    for (int i = 0; i < DEFAULT_CONTRIBUTORS.length; i++) {
      try {
        Class saveContribClass = JRClassLoader.loadClassForName(DEFAULT_CONTRIBUTORS[i]);
        Constructor<JRSaveContributor> constructor = saveContribClass
            .getConstructor(new Class[] { Locale.class, ResourceBundle.class });
        JRSaveContributor saveContrib = constructor.newInstance(new Object[] { getLocale(), this.resourceBundle });
        this.saveContributors.add(saveContrib);
      } catch (Exception exception) {
      }
    }
  }

  public void gotoHyperlink(JRPrintHyperlink hyperlink) {
    int page;
    switch (hyperlink.getHyperlinkTypeValue()) {
      case REFERENCE:
        if (isOnlyHyperlinkListener()) {
          System.out.println("Hyperlink reference : " + hyperlink.getHyperlinkReference());
          System.out.println("Implement your own JRHyperlinkListener to manage this type of event.");
        }
        break;

      case LOCAL_ANCHOR:
        if (hyperlink.getHyperlinkAnchor() != null) {
          Map anchorIndexes = this.jasperPrint.getAnchorIndexes();
          JRPrintAnchorIndex anchorIndex = (JRPrintAnchorIndex) anchorIndexes.get(hyperlink.getHyperlinkAnchor());
          if (anchorIndex.getPageIndex() != this.pageIndex) {
            setPageIndex(anchorIndex.getPageIndex());
            refreshPage();
          }
          Container container = this.pnlInScroll.getParent();
          if (container instanceof JViewport) {
            JViewport viewport = (JViewport) container;
            int newX = (int) (anchorIndex.getElementAbsoluteX() * this.realZoom);
            int newY = (int) (anchorIndex.getElementAbsoluteY() * this.realZoom);
            int maxX = this.pnlInScroll.getWidth() - viewport.getWidth();
            int maxY = this.pnlInScroll.getHeight() - viewport.getHeight();
            if (newX < 0)
              newX = 0;
            if (newX > maxX)
              newX = maxX;
            if (newY < 0)
              newY = 0;
            if (newY > maxY)
              newY = maxY;
            viewport.setViewPosition(new Point(newX, newY));
          }
        }
        break;

      case LOCAL_PAGE:
        page = this.pageIndex + 1;
        if (hyperlink.getHyperlinkPage() != null)
          page = hyperlink.getHyperlinkPage().intValue();
        if (page >= 1 && page <= this.jasperPrint.getPages().size() && page != this.pageIndex + 1) {
          setPageIndex(page - 1);
          refreshPage();
          Container container = this.pnlInScroll.getParent();
          if (container instanceof JViewport) {
            JViewport viewport = (JViewport) container;
            viewport.setViewPosition(new Point(0, 0));
          }
        }
        break;

      case REMOTE_ANCHOR:
        if (isOnlyHyperlinkListener()) {
          System.out.println("Hyperlink reference : " + hyperlink.getHyperlinkReference());
          System.out.println("Hyperlink anchor    : " + hyperlink.getHyperlinkAnchor());
          System.out.println("Implement your own JRHyperlinkListener to manage this type of event.");
        }
        break;

      case REMOTE_PAGE:
        if (isOnlyHyperlinkListener()) {
          System.out.println("Hyperlink reference : " + hyperlink.getHyperlinkReference());
          System.out.println("Hyperlink page      : " + hyperlink.getHyperlinkPage());
          System.out.println("Implement your own JRHyperlinkListener to manage this type of event.");
        }
        break;

      case CUSTOM:
        if (isOnlyHyperlinkListener()) {
          System.out.println("Hyperlink of type " + hyperlink.getLinkType());
          System.out.println("Implement your own JRHyperlinkListener to manage this type of event.");
        }
        break;
    }
  }

  protected boolean isOnlyHyperlinkListener() {
    int listenerCount;
    if (this.hyperlinkListeners == null) {
      listenerCount = 0;
    } else {
      listenerCount = this.hyperlinkListeners.size();
      if (this.hyperlinkListeners.contains(this))
        listenerCount--;
    }
    return (listenerCount == 0);
  }

  private void initComponents() {
    this.tlbToolBar = new JPanel();
    this.btnPrint = new JButton();
    this.btnPrint.setName("print");
    this.btnReload = new JButton();
    JPanel pnlSep01 = new JPanel();
    this.btnFirst = new JButton();
    this.btnPrevious = new JButton();
    this.btnNext = new JButton();
    this.btnLast = new JButton();
    this.txtGoTo = new JTextField();
    JPanel pnlSep02 = new JPanel();
    this.btnActualSize = new JToggleButton();
    this.btnFitPage = new JToggleButton();
    this.btnFitWidth = new JToggleButton();
    JPanel pnlSep03 = new JPanel();
    this.btnZoomIn = new JButton();
    this.btnZoomOut = new JButton();
    this.cmbZoom = new JComboBox();
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel();
    for (int i = 0; i < this.zooms.length; i++) {
      model.addElement("" + this.zooms[i] + "%");
    }
    this.cmbZoom.setModel(model);
    this.pnlMain = new JPanel();
    this.scrollPane = new JScrollPane();
    this.scrollPane.getHorizontalScrollBar().setUnitIncrement(5);
    this.scrollPane.getVerticalScrollBar().setUnitIncrement(5);
    this.pnlInScroll = new JPanel();
    this.pnlPage = new JPanel();
    JPanel jPanel4 = new JPanel();
    this.pnlLinks = new JPanel();
    JPanel jPanel5 = new JPanel();
    JPanel jPanel6 = new JPanel();
    JPanel jPanel7 = new JPanel();
    JPanel jPanel8 = new JPanel();
    JLabel jLabel1 = new JLabel();
    JPanel jPanel9 = new JPanel();
    this.lblPage = new PageRenderer(this);
    JPanel pnlStatus = new JPanel();
    this.lblStatus = new JLabel();
    setLayout(new BorderLayout());
    setMinimumSize(new Dimension(450, 150));
    setPreferredSize(new Dimension(450, 150));
    this.tlbToolBar.setLayout(new FlowLayout(0, 0, 2));
    this.tlbToolBar.setName("toolbar");
    this.btnPrint.setIcon(new ImageIcon(getClass().getResource("/net/sf/jasperreports/view/images/print.GIF")));
    this.btnPrint.setToolTipText(getBundleString("print"));
    this.btnPrint.setMargin(new Insets(2, 2, 2, 2));
    this.btnPrint.setMaximumSize(new Dimension(23, 23));
    this.btnPrint.setMinimumSize(new Dimension(23, 23));
    this.btnPrint.setPreferredSize(new Dimension(23, 23));
    this.btnPrint.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JSViewer.this.btnPrintActionPerformed(evt);
      }
    });
    this.btnPrint.addKeyListener(this.keyNavigationListener);
    this.tlbToolBar.add(this.btnPrint);
    pnlSep01.setMaximumSize(new Dimension(10, 10));
    this.tlbToolBar.add(pnlSep01);
    this.btnFirst.setIcon(new ImageIcon(getClass().getResource("/net/sf/jasperreports/view/images/first.GIF")));
    this.btnFirst.setToolTipText(getBundleString("first.page"));
    this.btnFirst.setMargin(new Insets(2, 2, 2, 2));
    this.btnFirst.setMaximumSize(new Dimension(23, 23));
    this.btnFirst.setMinimumSize(new Dimension(23, 23));
    this.btnFirst.setPreferredSize(new Dimension(23, 23));
    this.btnFirst.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JSViewer.this.btnFirstActionPerformed(evt);
      }
    });
    this.btnFirst.addKeyListener(this.keyNavigationListener);
    this.tlbToolBar.add(this.btnFirst);
    this.btnPrevious.setIcon(new ImageIcon(getClass().getResource("/net/sf/jasperreports/view/images/previous.GIF")));
    this.btnPrevious.setToolTipText(getBundleString("previous.page"));
    this.btnPrevious.setMargin(new Insets(2, 2, 2, 2));
    this.btnPrevious.setMaximumSize(new Dimension(23, 23));
    this.btnPrevious.setMinimumSize(new Dimension(23, 23));
    this.btnPrevious.setPreferredSize(new Dimension(23, 23));
    this.btnPrevious.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JSViewer.this.btnPreviousActionPerformed(evt);
      }
    });
    this.btnPrevious.addKeyListener(this.keyNavigationListener);
    this.tlbToolBar.add(this.btnPrevious);
    this.btnNext.setIcon(new ImageIcon(getClass().getResource("/net/sf/jasperreports/view/images/next.GIF")));
    this.btnNext.setToolTipText(getBundleString("next.page"));
    this.btnNext.setMargin(new Insets(2, 2, 2, 2));
    this.btnNext.setMaximumSize(new Dimension(23, 23));
    this.btnNext.setMinimumSize(new Dimension(23, 23));
    this.btnNext.setPreferredSize(new Dimension(23, 23));
    this.btnNext.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JSViewer.this.btnNextActionPerformed(evt);
      }
    });
    this.btnNext.addKeyListener(this.keyNavigationListener);
    this.tlbToolBar.add(this.btnNext);
    this.btnLast.setIcon(new ImageIcon(getClass().getResource("/net/sf/jasperreports/view/images/last.GIF")));
    this.btnLast.setToolTipText(getBundleString("last.page"));
    this.btnLast.setMargin(new Insets(2, 2, 2, 2));
    this.btnLast.setMaximumSize(new Dimension(23, 23));
    this.btnLast.setMinimumSize(new Dimension(23, 23));
    this.btnLast.setPreferredSize(new Dimension(23, 23));
    this.btnLast.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JSViewer.this.btnLastActionPerformed(evt);
      }
    });
    this.btnLast.addKeyListener(this.keyNavigationListener);
    this.tlbToolBar.add(this.btnLast);
    this.txtGoTo.setToolTipText(getBundleString("go.to.page"));
    this.txtGoTo.setMaximumSize(new Dimension(40, 23));
    this.txtGoTo.setMinimumSize(new Dimension(40, 23));
    this.txtGoTo.setPreferredSize(new Dimension(40, 23));
    this.txtGoTo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JSViewer.this.txtGoToActionPerformed(evt);
      }
    });
    this.txtGoTo.addKeyListener(this.keyNavigationListener);
    this.tlbToolBar.add(this.txtGoTo);
    pnlSep02.setMaximumSize(new Dimension(10, 10));
    this.tlbToolBar.add(pnlSep02);
    this.btnActualSize
        .setIcon(new ImageIcon(getClass().getResource("/net/sf/jasperreports/view/images/actualsize.GIF")));
    this.btnActualSize.setToolTipText(getBundleString("actual.size"));
    this.btnActualSize.setMargin(new Insets(2, 2, 2, 2));
    this.btnActualSize.setMaximumSize(new Dimension(23, 23));
    this.btnActualSize.setMinimumSize(new Dimension(23, 23));
    this.btnActualSize.setPreferredSize(new Dimension(23, 23));
    this.btnActualSize.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JSViewer.this.btnActualSizeActionPerformed(evt);
      }
    });
    this.btnActualSize.addKeyListener(this.keyNavigationListener);
    this.tlbToolBar.add(this.btnActualSize);

    this.btnFitPage.setIcon(new ImageIcon(getClass().getResource("/net/sf/jasperreports/view/images/fitpage.GIF")));
    this.btnFitPage.setToolTipText(getBundleString("fit.page"));
    this.btnFitPage.setMargin(new Insets(2, 2, 2, 2));
    this.btnFitPage.setMaximumSize(new Dimension(23, 23));
    this.btnFitPage.setMinimumSize(new Dimension(23, 23));
    this.btnFitPage.setPreferredSize(new Dimension(23, 23));
    this.btnFitPage.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JSViewer.this.btnFitPageActionPerformed(evt);
      }
    });
    this.btnFitPage.addKeyListener(this.keyNavigationListener);
    this.tlbToolBar.add(this.btnFitPage);
    this.btnFitWidth.setIcon(new ImageIcon(getClass().getResource("/net/sf/jasperreports/view/images/fitwidth.GIF")));
    this.btnFitWidth.setToolTipText(getBundleString("fit.width"));
    this.btnFitWidth.setMargin(new Insets(2, 2, 2, 2));
    this.btnFitWidth.setMaximumSize(new Dimension(23, 23));
    this.btnFitWidth.setMinimumSize(new Dimension(23, 23));
    this.btnFitWidth.setPreferredSize(new Dimension(23, 23));
    this.btnFitWidth.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JSViewer.this.btnFitWidthActionPerformed(evt);
      }
    });
    this.btnFitWidth.addKeyListener(this.keyNavigationListener);
    this.tlbToolBar.add(this.btnFitWidth);

    pnlSep03.setMaximumSize(new Dimension(10, 10));
    this.tlbToolBar.add(pnlSep03);

    this.btnZoomIn.setIcon(new ImageIcon(getClass().getResource("/net/sf/jasperreports/view/images/zoomin.GIF")));
    this.btnZoomIn.setToolTipText(getBundleString("zoom.in"));
    this.btnZoomIn.setMargin(new Insets(2, 2, 2, 2));
    this.btnZoomIn.setMaximumSize(new Dimension(23, 23));
    this.btnZoomIn.setMinimumSize(new Dimension(23, 23));
    this.btnZoomIn.setPreferredSize(new Dimension(23, 23));
    this.btnZoomIn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JSViewer.this.btnZoomInActionPerformed(evt);
      }
    });
    this.btnZoomIn.addKeyListener(this.keyNavigationListener);
    this.tlbToolBar.add(this.btnZoomIn);

    this.btnZoomOut.setIcon(new ImageIcon(getClass().getResource("/net/sf/jasperreports/view/images/zoomout.GIF")));
    this.btnZoomOut.setToolTipText(getBundleString("zoom.out"));
    this.btnZoomOut.setMargin(new Insets(2, 2, 2, 2));
    this.btnZoomOut.setMaximumSize(new Dimension(23, 23));
    this.btnZoomOut.setMinimumSize(new Dimension(23, 23));
    this.btnZoomOut.setPreferredSize(new Dimension(23, 23));
    this.btnZoomOut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JSViewer.this.btnZoomOutActionPerformed(evt);
      }
    });
    this.btnZoomOut.addKeyListener(this.keyNavigationListener);
    this.tlbToolBar.add(this.btnZoomOut);

    this.cmbZoom.setEditable(true);
    this.cmbZoom.setToolTipText(getBundleString("zoom.ratio"));
    this.cmbZoom.setMaximumSize(new Dimension(80, 23));
    this.cmbZoom.setMinimumSize(new Dimension(80, 23));
    this.cmbZoom.setPreferredSize(new Dimension(80, 23));
    this.cmbZoom.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JSViewer.this.cmbZoomActionPerformed(evt);
      }
    });
    this.cmbZoom.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        JSViewer.this.cmbZoomItemStateChanged(evt);
      }
    });
    this.cmbZoom.addKeyListener(this.keyNavigationListener);
    this.tlbToolBar.add(this.cmbZoom);

    add(this.tlbToolBar, "North");

    this.pnlMain.setLayout(new BorderLayout());
    this.pnlMain.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent evt) {
        JSViewer.this.pnlMainComponentResized(evt);
      }
    });

    this.scrollPane.setHorizontalScrollBarPolicy(32);
    this.scrollPane.setVerticalScrollBarPolicy(22);
    this.pnlInScroll.setLayout(new GridBagLayout());

    this.pnlPage.setLayout(new BorderLayout());
    this.pnlPage.setMinimumSize(new Dimension(100, 100));
    this.pnlPage.setPreferredSize(new Dimension(100, 100));

    jPanel4.setLayout(new GridBagLayout());
    jPanel4.setMinimumSize(new Dimension(100, 120));
    jPanel4.setPreferredSize(new Dimension(100, 120));
    this.pnlLinks.setLayout((LayoutManager) null);
    this.pnlLinks.setMinimumSize(new Dimension(5, 5));
    this.pnlLinks.setPreferredSize(new Dimension(5, 5));
    this.pnlLinks.setOpaque(false);
    this.pnlLinks.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent evt) {
        JSViewer.this.pnlLinksMousePressed(evt);
      }

      public void mouseReleased(MouseEvent evt) {
        JSViewer.this.pnlLinksMouseReleased(evt);
      }
    });
    this.pnlLinks.addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent evt) {
        JSViewer.this.pnlLinksMouseDragged(evt);
      }
    });

    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = 1;
    jPanel4.add(this.pnlLinks, gridBagConstraints);

    jPanel5.setBackground(Color.gray);
    jPanel5.setMinimumSize(new Dimension(5, 5));
    jPanel5.setPreferredSize(new Dimension(5, 5));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = 3;
    jPanel4.add(jPanel5, gridBagConstraints);

    jPanel6.setMinimumSize(new Dimension(5, 5));
    jPanel6.setPreferredSize(new Dimension(5, 5));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    jPanel4.add(jPanel6, gridBagConstraints);

    jPanel7.setBackground(Color.gray);
    jPanel7.setMinimumSize(new Dimension(5, 5));
    jPanel7.setPreferredSize(new Dimension(5, 5));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = 2;
    jPanel4.add(jPanel7, gridBagConstraints);

    jPanel8.setBackground(Color.gray);
    jPanel8.setMinimumSize(new Dimension(5, 5));
    jPanel8.setPreferredSize(new Dimension(5, 5));
    jLabel1.setText("jLabel1");

    jPanel8.add(jLabel1);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    jPanel4.add(jPanel8, gridBagConstraints);

    jPanel9.setMinimumSize(new Dimension(5, 5));
    jPanel9.setPreferredSize(new Dimension(5, 5));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    jPanel4.add(jPanel9, gridBagConstraints);

    this.lblPage.setBackground(Color.white);
    this.lblPage.setBorder(new LineBorder(new Color(0, 0, 0)));
    this.lblPage.setOpaque(true);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = 1;
    gridBagConstraints.weightx = 1.0D;
    gridBagConstraints.weighty = 1.0D;
    jPanel4.add(this.lblPage, gridBagConstraints);

    this.pnlPage.add(jPanel4, "Center");

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.insets = new Insets(5, 5, 5, 5);
    this.pnlInScroll.add(this.pnlPage, gridBagConstraints);

    this.scrollPane.setViewportView(this.pnlInScroll);
    this.pnlMain.add(this.scrollPane, "Center");
    add(this.pnlMain, "Center");

    pnlStatus.setLayout(new FlowLayout(1, 0, 0));

    this.lblStatus.setFont(new java.awt.Font("Dialog", 1, 10));
    this.lblStatus.setText("Page i of n");
    this.lblStatus.setName("message");
    pnlStatus.add(this.lblStatus);
    add(pnlStatus, "South");
    pnlStatus.setVisible(false);
    addKeyListener(this.keyNavigationListener);
  }

  void txtGoToActionPerformed(ActionEvent evt) {
    try {
      int pageNumber = Integer.parseInt(this.txtGoTo.getText());
      if (pageNumber != this.pageIndex + 1 && pageNumber > 0 && pageNumber <= this.jasperPrint
          .getPages().size()) {
        setPageIndex(pageNumber - 1);
        refreshPage();
      }
    } catch (NumberFormatException numberFormatException) {
    }
  }

  void cmbZoomItemStateChanged(ItemEvent evt) {
    this.btnActualSize.setSelected(false);
    this.btnFitPage.setSelected(false);
    this.btnFitWidth.setSelected(false);
  }

  void pnlMainComponentResized(ComponentEvent evt) {
    if (this.btnFitPage.isSelected()) {
      fitPage();
      this.btnFitPage.setSelected(true);
    } else if (this.btnFitWidth.isSelected()) {
      setRealZoomRatio(
          ((float) this.pnlInScroll.getVisibleRect().getWidth() - 20.0F) / this.jasperPrint.getPageWidth());
      this.btnFitWidth.setSelected(true);
    }
  }

  void btnActualSizeActionPerformed(ActionEvent evt) {
    if (this.btnActualSize.isSelected()) {
      this.btnFitPage.setSelected(false);
      this.btnFitWidth.setSelected(false);
      this.cmbZoom.setSelectedIndex(-1);
      setZoomRatio(1.0F);
      this.btnActualSize.setSelected(true);
    }
  }

  void btnFitWidthActionPerformed(ActionEvent evt) {
    if (this.btnFitWidth.isSelected()) {
      this.btnActualSize.setSelected(false);
      this.btnFitPage.setSelected(false);
      this.cmbZoom.setSelectedIndex(-1);
      setRealZoomRatio(
          ((float) this.pnlInScroll.getVisibleRect().getWidth() - 20.0F) / this.jasperPrint.getPageWidth());
      this.btnFitWidth.setSelected(true);
    }
  }

  void btnFitPageActionPerformed(ActionEvent evt) {
    if (this.btnFitPage.isSelected()) {
      this.btnActualSize.setSelected(false);
      this.btnFitWidth.setSelected(false);
      this.cmbZoom.setSelectedIndex(-1);
      fitPage();
      this.btnFitPage.setSelected(true);
    }
  }

  @ActionTool(target = ExportAction.class)
  public void export(ActionEvent evt) {
  }

  void pnlLinksMouseDragged(MouseEvent evt) {
    Container container = this.pnlInScroll.getParent();
    if (container instanceof JViewport) {

      JViewport viewport = (JViewport) container;
      Point point = viewport.getViewPosition();
      int newX = point.x - evt.getX() - this.downX;
      int newY = point.y - evt.getY() - this.downY;

      int maxX = this.pnlInScroll.getWidth() - viewport.getWidth();
      int maxY = this.pnlInScroll.getHeight() - viewport.getHeight();

      if (newX < 0) {
        newX = 0;
      }
      if (newX > maxX) {
        newX = maxX;
      }
      if (newY < 0) {
        newY = 0;
      }
      if (newY > maxY) {
        newY = maxY;
      }

      viewport.setViewPosition(new Point(newX, newY));
    }
  }

  void pnlLinksMouseReleased(MouseEvent evt) {
    this.pnlLinks.setCursor(new Cursor(0));
  }

  void pnlLinksMousePressed(MouseEvent evt) {
    this.pnlLinks.setCursor(new Cursor(13));
    this.downX = evt.getX();
    this.downY = evt.getY();
  }

  @ActionTool(target = PrintAction.class)
  public void btnPrintActionPerformed(ActionEvent evt) {
    X.alert(Integer.valueOf(33333));
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          JSViewer.this.btnPrint.setEnabled(false);
          JSViewer.this.setCursor(Cursor.getPredefinedCursor(3));
          JasperPrintManager.printReport(JSViewer.this.jasperPrint, true);
        } catch (Exception ex) {
          X.alert(JSViewer.this.getBundleString("error.printing"));
        } finally {
          JSViewer.this.setCursor(Cursor.getPredefinedCursor(0));
          JSViewer.this.btnPrint.setEnabled(true);
        }
      }
    });

    thread.start();
  }

  void btnLastActionPerformed(ActionEvent evt) {
    setPageIndex(this.jasperPrint.getPages().size() - 1);
    refreshPage();
  }

  void btnNextActionPerformed(ActionEvent evt) {
    setPageIndex(this.pageIndex + 1);
    refreshPage();
  }

  void btnPreviousActionPerformed(ActionEvent evt) {
    setPageIndex(this.pageIndex - 1);
    refreshPage();
  }

  void btnFirstActionPerformed(ActionEvent evt) {
    setPageIndex(0);
    refreshPage();
  }

  @ActionTool(target = RefreshAction.class)
  public void btnReloadActionPerformed(ActionEvent evt) {
    if (this.type == 1) {

      try {
        loadReport(this.reportFileName, this.isXML);
      } catch (JRException e) {

        this.jasperPrint = null;
        setPageIndex(0);
        refreshPage();

        JOptionPane.showMessageDialog(this, getBundleString("error.loading"));
      }

      forceRefresh();
    }
  }

  protected void forceRefresh() {
    this.zoom = 0.0F;
    this.realZoom = 0.0F;
    setZoomRatio(1.0F);
  }

  void btnZoomInActionPerformed(ActionEvent evt) {
    this.btnActualSize.setSelected(false);
    this.btnFitPage.setSelected(false);
    this.btnFitWidth.setSelected(false);
    int newZoomInt = (int) (100.0F * getZoomRatio());
    int index = Arrays.binarySearch(this.zooms, newZoomInt);
    if (index < 0) {
      setZoomRatio(this.zooms[-index - 1] / 100.0F);
    } else if (index < this.cmbZoom.getModel().getSize() - 1) {
      setZoomRatio(this.zooms[index + 1] / 100.0F);
    }
  }

  void btnZoomOutActionPerformed(ActionEvent evt) {
    this.btnActualSize.setSelected(false);
    this.btnFitPage.setSelected(false);
    this.btnFitWidth.setSelected(false);
    int newZoomInt = (int) (100.0F * getZoomRatio());
    int index = Arrays.binarySearch(this.zooms, newZoomInt);
    if (index > 0) {
      setZoomRatio(this.zooms[index - 1] / 100.0F);
    } else if (index < -1) {
      setZoomRatio(this.zooms[-index - 2] / 100.0F);
    }
  }

  void cmbZoomActionPerformed(ActionEvent evt) {
    float newZoom = getZoomRatio();
    if (newZoom < 0.5F) {
      newZoom = 0.5F;
    }
    if (newZoom > 10.0F) {
      newZoom = 10.0F;
    }
    setZoomRatio(newZoom);
  }

  void hyperlinkClicked(MouseEvent evt) {
    JPanel link = (JPanel) evt.getSource();
    JRPrintHyperlink element = (JRPrintHyperlink) this.linksMap.get(link);
    hyperlinkClicked(element);
  }

  protected void hyperlinkClicked(JRPrintHyperlink hyperlink){
    try{
        JRHyperlinkListener listener = null;
        for(int i=0;i<hyperlinkListeners.size();i++){
            listener = (JRHyperlinkListener)hyperlinkListeners.get(i);
            listener.gotoHyperlink(hyperlink);
        }
    }catch(JRException e){
        X.alert(getBundleString("error.hyperlink"));
    }
}

  public int getPageIndex() {
    return this.pageIndex;
  }

  private void setPageIndex(int index) {
    if (this.jasperPrint != null && this.jasperPrint.getPages() != null && this.jasperPrint.getPages().size() > 0) {
      if (index >= 0 && index < this.jasperPrint.getPages().size()) {
        this.pageIndex = index;
        this.pageError = false;
        this.btnFirst.setEnabled((this.pageIndex > 0));
        this.btnPrevious.setEnabled((this.pageIndex > 0));
        this.btnNext.setEnabled((this.pageIndex < this.jasperPrint.getPages().size() - 1));
        this.btnLast.setEnabled((this.pageIndex < this.jasperPrint.getPages().size() - 1));
        this.txtGoTo.setEnabled((this.btnFirst.isEnabled() || this.btnLast.isEnabled()));
        this.txtGoTo.setText("" + (this.pageIndex + 1));
        this.lblStatus.setText(MessageFormat.format(getBundleString("page"),
            new Object[] { Integer.valueOf(this.pageIndex + 1), Integer.valueOf(this.jasperPrint.getPages().size()) }));
      }
    } else {
      this.btnFirst.setEnabled(false);
      this.btnPrevious.setEnabled(false);
      this.btnNext.setEnabled(false);
      this.btnLast.setEnabled(false);
      this.txtGoTo.setEnabled(false);
      this.txtGoTo.setText("");
      this.lblStatus.setText("");
    }
  }

  public Object retrieve(Object... param) {

    try {
      this.store.retrieve(param);
      putClientProperty("#params", new XMap(new Object[] { "institucion", "fff" }));
      loadReport(this);
      return Boolean.valueOf(true);
    } catch (Exception e) {
      X.alert(e);
      return e;
    }

  }

  public void loadReport(Component aThis) throws JRException {
    loadReport(JR.getJasperPrint(aThis));
  }

  protected void loadReport(String fileName, boolean isXmlReport) throws JRException {
    if (isXmlReport) {
      this.jasperPrint = JRPrintXmlLoader.load(fileName);
    } else {
      this.jasperPrint = (JasperPrint) JRLoader.loadObjectFromFile(fileName);
    }
    this.type = 1;
    this.isXML = isXmlReport;
    this.reportFileName = fileName;
    this.fileResolver = new SimpleFileResolver(
        Arrays.asList(new File[] { (new File(fileName)).getParentFile(), new File(".") }));
    this.fileResolver.setResolveAbsolutePath(true);
    this.btnReload.setEnabled(true);
    setPageIndex(0);
  }

  protected void loadReport(InputStream is, boolean isXmlReport) throws JRException {
    if (isXmlReport) {
      this.jasperPrint = JRPrintXmlLoader.load(is);
    } else {
      this.jasperPrint = (JasperPrint) JRLoader.loadObject(is);
    }
    this.type = 2;
    this.isXML = isXmlReport;
    this.btnReload.setEnabled(false);
    setPageIndex(0);
  }

  public void loadReport(JasperPrint jrPrint) {
    this.jasperPrint = jrPrint;
    this.type = 3;
    this.isXML = false;
    this.btnReload.setEnabled(false);
    setPageIndex(0);
    refreshPage();
  }

  protected void refreshPage() {
    boolean renderImage = false;
    if (this.jasperPrint == null || this.jasperPrint.getPages() == null || this.jasperPrint.getPages().isEmpty()) {
      this.pnlPage.setVisible(false);

      this.btnPrint.setEnabled(false);
      this.btnActualSize.setEnabled(false);
      this.btnFitPage.setEnabled(false);
      this.btnFitWidth.setEnabled(false);
      this.btnZoomIn.setEnabled(false);
      this.btnZoomOut.setEnabled(false);
      this.cmbZoom.setEnabled(false);
      return;
    }
    this.pnlPage.setVisible(true);

    this.btnPrint.setEnabled(true);
    this.btnActualSize.setEnabled(true);
    this.btnFitPage.setEnabled(true);
    this.btnFitWidth.setEnabled(true);
    this.btnZoomIn.setEnabled((this.zoom < 10.0F));
    this.btnZoomOut.setEnabled((this.zoom > 0.5F));
    this.cmbZoom.setEnabled(true);
    Dimension dim = new Dimension((int) (this.jasperPrint.getPageWidth() * this.realZoom) + 8,
        (int) (this.jasperPrint.getPageHeight() * this.realZoom) + 8);
    this.pnlPage.setMaximumSize(dim);
    this.pnlPage.setMinimumSize(dim);
    this.pnlPage.setPreferredSize(dim);
    long maxImageSize = 100000L;

    if (maxImageSize <= 0L) {
      renderImage = false;
    } else {
      //long imageSize = JRPrinterAWT.getImageSize(this.jasperPrint, this.realZoom);
      //renderImage = (imageSize <= maxImageSize);
    }
    this.lblPage.setRenderImage(renderImage);
    if (renderImage)
      setPageImage();
    this.pnlLinks.removeAll();
    this.linksMap = new HashMap<>();
    createHyperlinks();
    if (!renderImage) {
      this.lblPage.setIcon((Icon) null);
      this.pnlMain.validate();
      this.pnlMain.repaint();
    }
  }

  protected void setPageImage() {
    Image image;
    if (this.pageError) {
      image = getPageErrorImage();
    } else {
      try {
        image = JasperPrintManager.printPageToImage(this.jasperPrint, this.pageIndex, this.realZoom);
      } catch (Exception e) {
        this.pageError = true;
        image = getPageErrorImage();
        X.alert(ResourceBundle.getBundle("net/sf/jasperreports/view/viewer").getString("error.displaying"));
      }
    }
    ImageIcon imageIcon = new ImageIcon(image);
    this.lblPage.setIcon(imageIcon);
  }

  protected Image getPageErrorImage(){
    Image image = new BufferedImage((int) (jasperPrint.getPageWidth() * realZoom) + 1,
    (int) (jasperPrint.getPageHeight() * realZoom) + 1,BufferedImage.TYPE_INT_RGB);
    Graphics2D grx = (Graphics2D) image.getGraphics();
    AffineTransform transform = new AffineTransform();
    transform.scale(realZoom, realZoom);
    grx.transform(transform);
    drawPageError(grx);
    return image;
}

  protected void createHyperlinks() {
    List<JRPrintPage> pages = this.jasperPrint.getPages();
    JRPrintPage page = pages.get(this.pageIndex);
    createHyperlinks(page.getElements(), 0, 0);
  }

  protected void createHyperlinks(List elements, int offsetX, int offsetY) {
    if (elements != null && elements.size() > 0) {
      for (Iterator<JRPrintElement> it = elements.iterator(); it.hasNext();) {
        JRPrintElement element = it.next();
        JRImageMapRenderer imageMap = null;
        if (element instanceof JRPrintImage) {
          JRRenderable renderer = (JRRenderable) ((JRPrintImage) element).getRenderer();
          if (renderer instanceof JRImageMapRenderer) {
            imageMap = (JRImageMapRenderer) renderer;
            if (!imageMap.hasImageAreaHyperlinks())
              imageMap = null;
          }
        }
        boolean hasImageMap = (imageMap != null);
        JRPrintHyperlink hyperlink = null;
        if (element instanceof JRPrintHyperlink) {
          hyperlink = (JRPrintHyperlink) element;
        }
        boolean hasHyperlink = (!hasImageMap && hyperlink != null
            && hyperlink.getHyperlinkTypeValue() != HyperlinkTypeEnum.NONE);
        boolean hasTooltip = (hyperlink != null && hyperlink.getHyperlinkTooltip() != null);
        if (hasHyperlink || hasImageMap || hasTooltip) {
          JPanel link;
          if (hasImageMap) {
            Rectangle renderingArea = new Rectangle(0, 0, element.getWidth(), element.getHeight());
            link = new ImageMapPanel(renderingArea, imageMap);
          } else {
            link = new JPanel();
            if (hasHyperlink)
              link.addMouseListener(this.mouseListener);
          }
          if (hasHyperlink)
            link.setCursor(new Cursor(12));
          link.setLocation(
              (int) ((element.getX() + offsetX) * this.realZoom),
              (int) ((element.getY() + offsetY) * this.realZoom));

          link.setSize(
              (int) (element.getWidth() * this.realZoom),
              (int) (element.getHeight() * this.realZoom));

          link.setOpaque(false);
          String toolTip = getHyperlinkTooltip(hyperlink);
          if (toolTip == null && hasImageMap)
            toolTip = "";
          link.setToolTipText(toolTip);
          this.pnlLinks.add(link);
          this.linksMap.put(link, element);
        }
        if (element instanceof JRPrintFrame) {
          JRPrintFrame frame = (JRPrintFrame) element;
          int frameOffsetX = offsetX + frame.getX() + frame.getLineBox().getLeftPadding().intValue();
          int frameOffsetY = offsetY + frame.getY() + frame.getLineBox().getTopPadding().intValue();
          createHyperlinks(frame.getElements(), frameOffsetX, frameOffsetY);
        }
      }
    }
  }

  public int insert(Object... values) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean update() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Store getStore() {
    return this.store;
  }

  public XView setStore(Store store) {
    this.store = store;
    putClientProperty(TableModel.class, store);
    return this;
  }

  public XView cancel() {
    return this;
  }

  public void setEditable(boolean b) {
  }

  public boolean isEditable() {
    return false;
  }

  public int getRowCount() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object getValueAt(int row, String fieldName) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int[] getSelectedRows() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public XView deleteRows(int[] index) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public XView setSelected(Object... row) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void configureEnviroment() {
  }

  public boolean startEnviroment() {
    return true;
  }

  protected class ImageMapPanel
      extends JPanel implements MouseListener /* , MouseMotionListener */ {
    private static final long serialVersionUID = 10200L;
    protected final List imageAreaHyperlinks;

    public ImageMapPanel(Rectangle renderingArea, JRImageMapRenderer imageMap) {
      try {
        this.imageAreaHyperlinks = imageMap.getImageAreaHyperlinks(renderingArea);
      } catch (JRException e) {
        throw new JRRuntimeException(e);
      }
      addMouseListener(this);
      //addMouseMotionListener(this);
    }

    public String getToolTipText(MouseEvent event) {
      String tooltip = null;
      JRPrintImageAreaHyperlink imageMapArea = getImageMapArea(event);
      if (imageMapArea != null)
        tooltip = JSViewer.this.getHyperlinkTooltip(imageMapArea.getHyperlink());
      if (tooltip == null)
        tooltip = super.getToolTipText(event);
      return tooltip;
    }

    public void mouseDragged(MouseEvent e) {
      JSViewer.this.pnlLinksMouseDragged(e);
    }

    public void mouseMoved(MouseEvent e) {
      JRPrintImageAreaHyperlink imageArea = getImageMapArea(e);
      if (imageArea != null && imageArea
          .getHyperlink().getHyperlinkTypeValue() != HyperlinkTypeEnum.NONE) {
        e.getComponent().setCursor(Cursor.getPredefinedCursor(12));
      } else {
        e.getComponent().setCursor(Cursor.getDefaultCursor());
      }
    }

    protected JRPrintImageAreaHyperlink getImageMapArea(MouseEvent e) {
      return getImageMapArea((int) (e.getX() / JSViewer.this.realZoom), (int) (e.getY() / JSViewer.this.realZoom));
    }

    protected JRPrintImageAreaHyperlink getImageMapArea(int x, int y) {
      JRPrintImageAreaHyperlink image = null;
      if (this.imageAreaHyperlinks != null)
        for (ListIterator<JRPrintImageAreaHyperlink> it = this.imageAreaHyperlinks
            .listIterator(this.imageAreaHyperlinks.size()); image == null && it.hasPrevious();) {
          JRPrintImageAreaHyperlink area = it.previous();
          if (area.getArea().containsPoint(x, y)) {
            image = area;
          }
        }
      return image;
    }

    public void mouseClicked(MouseEvent e) {
      JRPrintImageAreaHyperlink imageMapArea = getImageMapArea(e);
      if (imageMapArea != null)
        JSViewer.this.hyperlinkClicked(imageMapArea.getHyperlink());
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
      e.getComponent().setCursor(Cursor.getPredefinedCursor(13));
      JSViewer.this.pnlLinksMousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
      e.getComponent().setCursor(Cursor.getDefaultCursor());
      JSViewer.this.pnlLinksMouseReleased(e);
    }
  }

  protected String getHyperlinkTooltip(JRPrintHyperlink hyperlink) {
    String toolTip = hyperlink.getHyperlinkTooltip();
    if (toolTip == null)
      toolTip = getFallbackTooltip(hyperlink);
    return toolTip;
  }

  protected String getFallbackTooltip(JRPrintHyperlink hyperlink) {
    String toolTip = null;
    switch (hyperlink.getHyperlinkTypeValue()) {
      case REFERENCE:
        toolTip = hyperlink.getHyperlinkReference();
        break;

      case LOCAL_ANCHOR:
        if (hyperlink.getHyperlinkAnchor() != null) {
          toolTip = "#" + hyperlink.getHyperlinkAnchor();
        }
        break;
      case LOCAL_PAGE:
        if (hyperlink.getHyperlinkPage() != null) {
          toolTip = "#page " + hyperlink.getHyperlinkPage();
        }
        break;
      case REMOTE_ANCHOR:
        toolTip = "";
        if (hyperlink.getHyperlinkReference() != null)
          toolTip = toolTip + hyperlink.getHyperlinkReference();
        if (hyperlink.getHyperlinkAnchor() != null) {
          toolTip = toolTip + "#" + hyperlink.getHyperlinkAnchor();
        }
        break;
      case REMOTE_PAGE:
        toolTip = "";
        if (hyperlink.getHyperlinkReference() != null)
          toolTip = toolTip + hyperlink.getHyperlinkReference();
        if (hyperlink.getHyperlinkPage() != null) {
          toolTip = toolTip + "#page " + hyperlink.getHyperlinkPage();
        }
        break;
    }

    return toolTip;
  }

  private void emptyContainer(Container container) {
    Component[] components = container.getComponents();
    if (components != null)
      for (int i = 0; i < components.length; i++) {
        if (components[i] instanceof Container) {
          emptyContainer((Container) components[i]);
        }
      }
    components = null;
    container.removeAll();
    container = null;
  }

  private float getZoomRatio() {
    float newZoom = this.zoom;

    try {
      newZoom = this.zoomDecimalFormat.parse(String.valueOf(this.cmbZoom.getEditor().getItem())).floatValue() / 100.0F;
    } catch (ParseException parseException) {
    }
    return newZoom;
  }

  public void setZoomRatio(float newZoom) {
    if (newZoom > 0.0F) {
      this.cmbZoom.getEditor().setItem(this.zoomDecimalFormat
          .format((newZoom * 100.0F)) + "%");

      if (this.zoom != newZoom) {
        this.zoom = newZoom;
        this.realZoom = this.zoom * this.screenResolution / 72.0F;
        refreshPage();
      }
    }
  }

  private void setRealZoomRatio(float newZoom) {
    if (newZoom > 0.0F && this.realZoom != newZoom) {
      this.zoom = newZoom * 72.0F / this.screenResolution;
      this.realZoom = newZoom;
      this.cmbZoom.getEditor().setItem(this.zoomDecimalFormat
          .format((this.zoom * 100.0F)) + "%");

      refreshPage();
    }
  }

  public void setFitWidthZoomRatio() {
    setRealZoomRatio(((float) this.pnlInScroll.getVisibleRect().getWidth() - 20.0F) / this.jasperPrint.getPageWidth());
  }

  public void setFitPageZoomRatio() {
    setRealZoomRatio(
        ((float) this.pnlInScroll.getVisibleRect().getHeight() - 20.0F) / this.jasperPrint.getPageHeight());
  }

  protected JRGraphics2DExporter getGraphics2DExporter() throws JRException {
    return new JRGraphics2DExporter();
  }

  protected void paintPage(Graphics2D grx) {
    if (this.pageError) {
      paintPageError(grx);
      return;
    }
    try {
      if (this.exporter == null) {
        this.exporter = getGraphics2DExporter();
      } else {
        this.exporter.reset();
      }
      this.exporter.setParameter(JRExporterParameter.JASPER_PRINT, this.jasperPrint);
      this.exporter.setParameter(JRGraphics2DExporterParameter.GRAPHICS_2D, grx.create());
      this.exporter.setParameter(JRExporterParameter.PAGE_INDEX, Integer.valueOf(this.pageIndex));
      exporter.setParameter(JRGraphics2DExporterParameter.ZOOM_RATIO, realZoom);
      this.exporter.setParameter(JRExporterParameter.OFFSET_X, Integer.valueOf(1));
      this.exporter.setParameter(JRExporterParameter.OFFSET_Y, Integer.valueOf(1));
      if (this.type == 1)
        ;

      this.exporter.exportReport();
    } catch (Exception e) {

      this.pageError = true;
      paintPageError(grx);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          X.alert(JSViewer.this.getBundleString("error.displaying"));
        }
      });
    }
  }

  protected void paintPageError(Graphics2D grx) {
    AffineTransform origTransform = grx.getTransform();
    AffineTransform transform = new AffineTransform();
    transform.translate(1.0D, 1.0D);
    transform.scale(this.realZoom, this.realZoom);
    grx.transform(transform);
    try {
      drawPageError(grx);
    } finally {
      grx.setTransform(origTransform);
    }
  }

  protected void drawPageError(Graphics grx) {
    grx.setColor(Color.white);
    grx.fillRect(0, 0, this.jasperPrint.getPageWidth() + 1, this.jasperPrint.getPageHeight() + 1);
  }

  protected void keyNavigate(KeyEvent evt) {
    boolean refresh = true;
    switch (evt.getKeyCode()) {
      case 34:
      case 40:
        dnNavigate(evt);
        break;
      case 33:
      case 38:
        upNavigate(evt);
        break;
      case 36:
        homeEndNavigate(0);
        break;
      case 35:
        homeEndNavigate(this.jasperPrint.getPages().size() - 1);
        break;
      default:
        refresh = false;
        break;
    }
    if (refresh)
      refreshPage();
  }

  private void dnNavigate(KeyEvent evt) {
    int bottomPosition = this.scrollPane.getVerticalScrollBar().getValue();
    this.scrollPane.dispatchEvent(evt);
    if ((this.scrollPane.getViewport().getHeight() > this.pnlPage.getHeight() || this.scrollPane
        .getVerticalScrollBar().getValue() == bottomPosition) && this.pageIndex < this.jasperPrint
            .getPages().size() - 1) {
      setPageIndex(this.pageIndex + 1);
      if (this.scrollPane.isEnabled())
        this.scrollPane.getVerticalScrollBar().setValue(0);
    }
  }

  private void upNavigate(KeyEvent evt) {
    if ((this.scrollPane.getViewport().getHeight() > this.pnlPage.getHeight() || this.scrollPane
        .getVerticalScrollBar().getValue() == 0) && this.pageIndex > 0) {

      setPageIndex(this.pageIndex - 1);
      if (this.scrollPane.isEnabled()) {
        this.scrollPane.getVerticalScrollBar().setValue(this.scrollPane.getVerticalScrollBar().getMaximum());
      }
    } else {
      this.scrollPane.dispatchEvent(evt);
    }
  }

  private void homeEndNavigate(int pageNumber) {
    setPageIndex(pageNumber);
    if (this.scrollPane.isEnabled())
      this.scrollPane.getVerticalScrollBar().setValue(0);
  }

  private void fitPage() {
    float heightRatio = ((float) this.pnlInScroll.getVisibleRect().getHeight() - 20.0F)
        / this.jasperPrint.getPageHeight();
    float widthRatio = ((float) this.pnlInScroll.getVisibleRect().getWidth() - 20.0F) / this.jasperPrint.getPageWidth();
    setRealZoomRatio((heightRatio < widthRatio) ? heightRatio : widthRatio);
  }

  class PageRenderer extends JLabel {
    private static final long serialVersionUID = 10200L;
    private boolean renderImage;
    JSViewer viewer = null;

    public PageRenderer(JSViewer viewer) {
      this.viewer = viewer;
    }

    public void paintComponent(Graphics g) {
      if (isRenderImage()) {
        super.paintComponent(g);
      } else {
        this.viewer.paintPage((Graphics2D) g.create());
      }
    }

    public boolean isRenderImage() {
      return this.renderImage;
    }

    public void setRenderImage(boolean renderImage) {
      this.renderImage = renderImage;
    }
  }
}

/*
 * Location:
 * /Users/ealarcop/Documents/ib-report.jar!/org/isobit/swing/JSViewer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version: 1.1.3
 */