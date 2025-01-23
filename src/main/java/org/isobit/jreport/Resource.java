package org.isobit.jreport;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.annotation.PostConstruct;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.core.layout.LayoutManager;
import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DJValueFormatter;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Path("")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class Resource {

	@PostConstruct
	void init() {
		/*
		 * JarFile jarFile = new JarFile("libs"); Enumeration<JarEntry> e =
		 * jarFile.entries();
		 * 
		 * URL[] urls = { new URL("jar:file:" + pathToJar+"!/") }; URLClassLoader cl =
		 * URLClassLoader.newInstance(urls);
		 * 
		 * while (e.hasMoreElements()) { JarEntry je = e.nextElement();
		 * if(je.isDirectory() || !je.getName().endsWith(".class")){ continue; } // -6
		 * because of .class String className =
		 * je.getName().substring(0,je.getName().length()-6); className =
		 * className.replace('/', '.'); Class c = cl.loadClass(className); }
		 */
		File ext = new File("ext");
		if (ext.exists())
			for (File jar : ext.listFiles()) {
				System.out.println(jar);
				try {
					File file = jar;
					URL url = file.toURI().toURL();

					URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
					Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
					method.setAccessible(true);
					method.invoke(classLoader, url);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				try {
					new URLClassLoader(new URL[] { jar.toURI().toURL() }, this.getClass().getClassLoader());
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
	}

	@ConfigProperty(name = "uploadDir")
	private String uploadDir;

	public Optional<String> getExtensionByStringHandling(String filename) {
		return Optional.ofNullable(filename).filter(f -> f.contains("."))
				.map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}

	public DynamicReport buildReport() throws Exception {

		/**
		 * Creates the DynamicReportBuilder and sets the basic options for 50 * the
		 * report 51
		 */
		FastReportBuilder drb = new FastReportBuilder();
		drb.addColumn("State", "state", String.class.getName(), 30)
				.addColumn("Branch", "branch", String.class.getName(), 30)
				.addColumn("Product Line", "productLine", String.class.getName(), 50)
				.addColumn("Item", "item", String.class.getName(), 50)
				.addColumn("Item Code", "id", Long.class.getName(), 30, true)
				.addColumn("Quantity", "quantity", Long.class.getName(), 60, true)
				.addColumn("Amount", "amount", Float.class.getName(), 70, true).addGroups(2)
				.setTitle("November sales report").setSubtitle("This report was generated at " + new Date())
				.setPrintBackgroundOnOddRows(true).setUseFullPageWidth(true);

		drb.addGlobalFooterVariable(drb.getColumn(4), DJCalculation.COUNT, null, new DJValueFormatter() {

			public String getClassName() {
				return String.class.getName();
			}

			public Object evaluate(Object value, Map fields, Map variables, Map parameters) {
				return (value == null ? "0" : value.toString()) + " Clients";
			}
		});

		DynamicReport dr = drb.build();

		return dr;
	}

	Map params = new HashMap();

	protected JasperPrint jp;
	private JasperReport jr;

	private abstract class Converter {
		abstract Object convert(Object o);
	}

	@POST
	@Path("dynamic")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	// @Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Object sendMultipartData2(@MultipartForm MultipartBody data) throws Exception {
		String extension = data.extension;

		List<Map> dummyCollection = new ArrayList();
		DynamicReport dr = null;
		System.out.println(data.filename);
		if (data.filename != null && data.filename.endsWith("json")) {
			File file = new File("tmp.json");
			try {
				InputStream inputStream = data.file;
				OutputStream outStream = new FileOutputStream(file);
				byte[] buffer = new byte[8 * 1024];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, bytesRead);
				}
				IOUtils.closeQuietly(outStream);

				Jsonb jsonb = JsonbBuilder.create();

				Object p = jsonb.fromJson(new FileInputStream(file), Object.class);
				FastReportBuilder drb = new FastReportBuilder();
				System.out.println(p.getClass());
				drb.setPrintBackgroundOnOddRows(true).setUseFullPageWidth(true);

				HashMap types = new HashMap();
				types.put("string", String.class.getName());
				types.put("long", Long.class.getName());
				types.put("int", Integer.class.getName());
				types.put("number", Number.class.getName());
				types.put("float", Float.class.getName());
				HashMap<String, Converter> converter = new HashMap();

				converter.put("string", new Converter() {
					@Override
					Object convert(Object o) {
						return o != null ? o.toString() : null;
					}
				});
				converter.put("long", new Converter() {
					@Override
					Object convert(Object o) {
						return o != null ? ((Number) o).longValue() : null;
					}
				});
				converter.put("int", new Converter() {
					@Override
					Object convert(Object o) {
						return o != null ? ((Number) o).intValue() : null;
					}
				});
				converter.put("number", new Converter() {
					@Override
					Object convert(Object o) {
						return o;
					}
				});
				converter.put("float", new Converter() {
					@Override
					Object convert(Object o) {
						return o != null ? ((Number) o).floatValue() : null;
					}
				});
				converter.put("double", new Converter() {
					@Override
					Object convert(Object o) {
						return o != null ? ((Number) o).doubleValue() : null;
					}
				});
				HashMap<String, Converter> fieldConverter = new HashMap();

				if (p instanceof Map) {
					params.putAll((Map) p);
					List<Map> columns = (List) params.remove("columns");
					if (params.containsKey("title"))
						drb.setTitle(params.remove("title").toString());
					for (Map column : columns) {
						fieldConverter.put(column.get("name").toString(), converter.get(column.get("type")));
						Style dataStyle = new Style();
						Object pattern = column.get("pattern");
						if (pattern != null)
							dataStyle.setPattern(pattern.toString());
						Object blankWhenNull = column.get("pattern");
						if (blankWhenNull != null)
							dataStyle.setBlankWhenNull(Boolean.parseBoolean(blankWhenNull.toString()));
						Object horizontalAlign = column.get("horizontalAlign");
						if (horizontalAlign != null) {
							switch (horizontalAlign.toString().toLowerCase()) {
								case "center":
									dataStyle.setHorizontalAlign(HorizontalAlign.CENTER);
									break;
								case "left":
									dataStyle.setHorizontalAlign(HorizontalAlign.LEFT);
									break;
								case "right":
									dataStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
									break;
								case "justify":
									dataStyle.setHorizontalAlign(HorizontalAlign.JUSTIFY);
							}

						}

						/*
						 * dataStyle.setFont(Font.ARIAL_SMALL);
						 */
						drb.addColumn(column.get("name").toString(), column.get("name").toString(),
								types.get(column.get("type")).toString(),
								column.containsKey("width") ? Integer.parseInt(column.get("width").toString()) : 30,
								dataStyle);
					}
					dr = drb.build();
					List<Map> data2 = (List) params.remove("data");
					for (Map m : data2) {
						for (String k : fieldConverter.keySet()) {
							Object oo = m.get(k);
							if (oo != null) {
								m.put(k, fieldConverter.get(k).convert(oo));
							}
						}
					}
					params.put(DataSource.class, data2);
				}
				System.out.println(dr);
				if (p instanceof List) {
					params.put(DataSource.class, (List) p);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			dr = buildReport();
			Random random = new Random();
			List states = new ArrayList();
			states.add("PERU");
			states.add("EEUU");
			states.add("ITALIA");
			states.add("CHILE");
			states.add("MEXICO");
			states.add("EJIPTO");
			List branchs = new ArrayList();
			for (int i = 0; i < 10; i++) {
				// branchs.add(RandomUtil.getW(18, true, 2));
			}
			for (int i = 0; i < 100; i++) {
				/*
				 * HashMap hm = new HashMap();
				 * hm.put("state", RandomUtil.get(states));
				 * hm.put("branch", RandomUtil.get(branchs));
				 * hm.put("productLine", RandomUtil.getW(50, true, 20));
				 * hm.put("item", RandomUtil.getCode(10));
				 * hm.put("id", (long) RandomUtil.getNumber(0, 3000, 0));
				 * hm.put("quantity", (long) RandomUtil.getNumber(0, 3000, 0));
				 * hm.put("amount", (float) RandomUtil.getNumber(0, 3000, 2));
				 * dummyCollection.add(hm);
				 */
			}
			Collections.sort(dummyCollection, (a, b) -> {
				int i = a.get("state").toString().compareToIgnoreCase(b.get("state").toString());
				if (i == 0)
					i = a.get("branch").toString().compareToIgnoreCase(b.get("branch").toString());
				return i;
			});
			params.put(DataSource.class, dummyCollection);
		}

		JR.setUPLOAD_DIR(uploadDir);
		jr = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), params);

		params.put(JR.EXTENSION, extension);
		Object o = org.isobit.jreport.JR.open(jr, params);
		String filename = "report-" + new Date().getTime() + "." + extension;
		return javax.ws.rs.core.Response.ok(o, MediaType.APPLICATION_OCTET_STREAM)
				.header("content-disposition", "attachment; filename = " + filename).build();
	}

	/*
	 * protected void exportReport() throws Exception {
	 * ReportExporter.exportReport(jp, System.getProperty("user.dir") +
	 * "/target/reports/" + this.getClass().getName() + ".pdf"); exportToJRXML(); }
	 * 
	 * protected void exportToJRXML() throws Exception { if (this.jr != null) {
	 * DynamicJasperHelper.generateJRXML(this.jr, "UTF-8",
	 * System.getProperty("user.dir") + "/target/reports/" +
	 * this.getClass().getName() + ".jrxml");
	 * 
	 * } else { DynamicJasperHelper.generateJRXML(this.dr, this.getLayoutManager(),
	 * this.params, "UTF-8", System.getProperty("user.dir") + "/target/reports/" +
	 * this.getClass().getName() + ".jrxml"); } }
	 * 
	 * protected void exportToHTML() throws Exception {
	 * ReportExporter.exportReportHtml(this.jp, System.getProperty("user.dir") +
	 * "/target/reports/" + this.getClass().getName() + ".html"); }
	 */
	@POST
	@Path("")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	// @Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Object sendMultipartData(@MultipartForm MultipartBody data) {

		Map m = new HashMap();
		String filename = "" + data.filename;
		String output = data.output;
		// X.log("data.filename=" + data.filename);
		InputStream inputStream = data.file;
		data.extension = data.extension != null ? data.extension : "pdf";
		m.put(JR.EXTENSION, data.extension);
		// InputStreamReader inputStreamReader = new InputStreamReader(initialStream);

		JR.setUPLOAD_DIR(uploadDir);
		String jasperFile = data.template;
		if (!jasperFile.endsWith(".jasper"))
			jasperFile = jasperFile + ".jasper";

		if (filename.endsWith("json")) {
			File file = new File("tmp.json");
			/* Stream<String> reader= new BufferedReader(inputStreamReader).lines(); */

			/*
			 * try (OutputStreamWriter writer = new OutputStreamWriter(new
			 * FileOutputStream(file), StandardCharsets.UTF_8)) {
			 * writer.write(reader.collect(Collectors.joining())); }catch (IOException e) {
			 * // TODO Auto-generated catch block e.printStackTrace(); }
			 */

			/*
			 * final BufferedWriter writer; String line; try ( writer =
			 * Files.newBufferedWriter(dst, StandardCharsets.UTF_8); ) { while ((line =
			 * reader.readLine()) != null) { writer.write(line); writer.newLine(); } }
			 */
			try {
				OutputStream outStream = new FileOutputStream(file);
				byte[] buffer = new byte[8 * 1024];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, bytesRead);
				}
				IOUtils.closeQuietly(outStream);
				System.out.println(file.getAbsolutePath());
				Jsonb jsonb = JsonbBuilder.create();
				Object p = jsonb.fromJson(new FileInputStream(file), Object.class);

if(data.orihinal!=null){
m.put(DataSource.class, file);
}else
				if (p instanceof Map) {
					m.putAll((Map) p);
					m.put(DataSource.class, m.remove("data"));
					jsonb.toJson(m.get(DataSource.class), new FileOutputStream(file));
					m.put(DataSource.class, file);
					System.out.println(m.get(DataSource.class));
					System.out.println(m.keySet());
				}else if (p instanceof List) {
					System.out.println("===="+new ObjectMapper().writeValueAsString(p));
					m.put(DataSource.class, (List) p);
				} else
					m.put(DataSource.class, file);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				ObjectInputStream ois = new ObjectInputStream(inputStream);
				Map map = (Map) ois.readObject();
				m.putAll(map);
				m.put(DataSource.class, map.get("data"));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if (inputStream != null)
			IOUtils.closeQuietly(inputStream);

		m.put("rest", true);
		System.out.println(jasperFile.toString());
		Object o = org.isobit.jreport.JR.open(jasperFile.toString(), m);
		if (output == null) {
			output = data.template + "." + data.extension;
		}

		filename = "report-" + new Date().getTime();
		return javax.ws.rs.core.Response.ok(o, MediaType.APPLICATION_OCTET_STREAM)
				.header("content-disposition", "attachment; filename = " + output).build();
	}
	/*
	 * Map m = new HashMap(); String tempFile = (String) params.get("tempFile"); int
	 * size = XUtil.intValue(params.get("size")); String type = (String)
	 * XUtil.isEmpty(params.get("type"), " "); String template = (String)
	 * params.get("template"); m.putAll(params); String jr = (template != null ?
	 * template : "Siaf_Analisis_Clasificador_Meta_A4_Landscape") + ".jasper"; File
	 * jasperFile = new File(getUploadDir() + "\\jasper\\" + jr); if
	 * (!jasperFile.exists()) { jasperFile = new
	 * File("E:\\projects\\jasperreports\\MyReports\\" + jr); } if
	 * (!jasperFile.exists()) { throw new RuntimeException(jasperFile +
	 * " no existe!"); } File file; if (tempFile == null) { file = new
	 * File("tmp.json"); try (OutputStreamWriter writer = new OutputStreamWriter(new
	 * FileOutputStream(file), StandardCharsets.UTF_8)) { writer.write((String)
	 * params.get("data")); } //System.out.println("file=" +
	 * file.getAbsolutePath()); m.put(DataSource.class, file);
	 * //System.out.println(params.get("data")); //Se quizo enviar el json as string
	 * //El problema es q si no se tiene un pojo definido no se puede reconocer los
	 * tipos correctos //por ejemplo las fechas //m.put(DataSource.class,
	 * params.get("data")); } else { file = new
	 * File(File.createTempFile("temp-file-name", ".tmp").getParentFile(),
	 * tempFile);
	 * 
	 * m.put(DataSource.class, file); } m.put("rest", true); Object o =
	 * org.isobit.jreport.JR.open(jasperFile.toString(), m); String filename =
	 * "report-" + new Date().getTime(); return Response .ok(o,
	 * MediaType.APPLICATION_OCTET_STREAM) .header("content-disposition",
	 * "attachment; filename = " + filename+"."+params.get("-EXTENSION")) .build();
	 */

}