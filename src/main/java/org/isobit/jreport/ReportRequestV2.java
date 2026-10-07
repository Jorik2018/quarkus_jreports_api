package org.isobit.jreport;

import java.util.Map;

public class ReportRequestV2 {

    public String template;
    public String extension;
    public String output;

    public Map<String, Object> parameters;

    public Object data;
}