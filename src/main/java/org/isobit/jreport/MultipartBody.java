package org.isobit.jreport;

import java.io.InputStream;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class MultipartBody {

    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream file;

    @FormParam("template")
    @PartType(MediaType.TEXT_PLAIN)   
    public String template;
    
    @FormParam("extension")
    @PartType(MediaType.TEXT_PLAIN)   
    public String extension;

    @FormParam("filename")
    @PartType(MediaType.TEXT_PLAIN)
    public String filename;
    
    @FormParam("output")
    @PartType(MediaType.TEXT_PLAIN)
    public String output;
    
    @FormParam("page")
    @PartType(MediaType.TEXT_PLAIN)
    public Integer page;
    
}
