package com.neotys.xebialabs.xl;


import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.validation.ValidationMessage;
import com.xebialabs.overthere.*;
import com.xebialabs.xltype.serialization.xstream.Converters;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;

import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.Configuration;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;

import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;
import com.xebialabs.overthere.util.OverthereUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import scala.sys.process.processInternal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_USERNAME;
import static com.xebialabs.overthere.ssh.SshConnectionType.*;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;
import  com.neotys.nls.security.tools.PasswordEncoder;
/**
 * Created by hrexed on 04/12/17.
 */
public class NeoLoadTest  {

    private static final String SCRIPT_NAME = "NeoLoad-Test";
    private static final String LINUX="Linux";
    private static final String WINDOWS="Windows";
    private static final String MACOS="MacOs";
    private static final String HOMEWIndows="%HOMEDRIVE%%HOMEPATH% ";
    private static final String HomeUnix="~";
    private static final String UNixScpHome="/home/";
    private static final String MacScpHome="/Users/";
    private  ConnectionOptions options;
    private  String OS;
    private  String NLInstallationPath;
    private  String NLProjectPath;
    private  String NLScenarioName;
    private  String NLHost;
    private  String NLUsername;
    private  String NLPassword;
    private  boolean NLIsCollab = false;
    private  String NLCollabUrl;
    private  String NLCollabUsername;
    private  String NLCollabProjectPath;
    private  String NLCollabPassword;
    private  String NTSUrl;
    private  String NTSUsername;
    private  String NTSPassword;
    private  String Nbhour;
    private  String NbVU;
    private  boolean NLIsNTS;
    private  String NTSLicenseID;
    private  CapturingOverthereExecutionOutputHandler stdout = capturingHandler();
    private  CapturingOverthereExecutionOutputHandler stderr = capturingHandler();
    private  String NLCollabProjectName;
    private  String NLWEBURL;
    private  String NLWEBAPIToken;
    private String NLCloudUsername;
    private String NLCloudPassword;
    private boolean NLIsCloud;
    private  boolean NlISNLWeb;
    private  String NLTestDescription;
    private final static   String SPACE=" ";
    private String ReleaseName;
    private String ReleaseId;
    private String CloudYML=null;
    private String Variables=null;
    private boolean IsVariableUsed=false;
    private boolean IsCloudUsed=false;

    public NeoLoadTest(String Host, String Username,String password, String path, String Os,String NeoloadScenario ,String LocalNeoLoadProject,String  NeoLoadTestDescription ,String NeoloadWebAPItoken, String NLCollabProjectName, String CollaborationProjectPath,String nbVU ,String nbhour)
    {

        this.NLHost=Host;
        this.NLUsername=Username;
        this.NLPassword=password;
        this.NLInstallationPath=path;
        this.OS=Os;
        this.NLScenarioName=NeoloadScenario;
        this.NLProjectPath=LocalNeoLoadProject;
        this.NLTestDescription=NeoLoadTestDescription;
        this.NLWEBAPIToken=NeoloadWebAPItoken;
        this.NLCollabProjectPath=CollaborationProjectPath;
        this.NLCollabProjectName=NLCollabProjectName;
        this.NLIsCollab=false;
        this.NLIsNTS=false;
        this.NlISNLWeb=false;
        this.NLIsCloud=false;
        this.Nbhour=nbhour;
        this.NbVU=nbVU;
        options = new ConnectionOptions();


    }
    public void SetReleaseInformation(String id, String name)
    {
        this.ReleaseId=id;
        this.ReleaseName=name;
    }
    public void AddCloudYML(String yml)
    {
        if(yml !=null) {
            this.CloudYML = yml;
            IsCloudUsed = true;
        }
    }

    public void SetVarialbe(String Variable)
    {
        if(Variable!=null) {
            Variables = Variable;
            IsVariableUsed = true;
        }
    }
    public void SetNLCloud(String User,String pass)
    {
        this.NLCloudPassword=pass;
        this.NLCloudUsername=User;
        this.NLIsCloud=true;
    }
    public void SetWebAPI(String WebURL)
    {
        this.NLWEBURL=WebURL;
        this.NlISNLWeb=true;
    }
    public void SetNTS(String url,String username, String password,String licenseID)
    {
        this.NLIsNTS=true;
        this.NTSUrl=url;
        this.NTSUsername=username;
        this.NTSPassword=password;
        this.NTSLicenseID=licenseID;
    }
    public void SetCollab(String url,String username,String password)
    {
        this.NLIsCollab=true;
        this.NLCollabUrl=url;
        this.NLCollabUsername=username;
        this.NLCollabPassword=password;
    }

    private String GenerateLocalTempFolder()
    {
       if(this.OS.equalsIgnoreCase(WINDOWS))
               return HOMEWIndows+"\\";
        else
            return HomeUnix+"/";

    }


    private String GenerateFileTempFolder()
    {
        if(this.OS.equalsIgnoreCase(WINDOWS))
            return HOMEWIndows+"\\";
        else {
            if(this.OS.equalsIgnoreCase(LINUX)) {
                if(this.NLUsername.equalsIgnoreCase("root"))
                    return UNixScpHome + "/";
                else
                    return UNixScpHome + "/" + this.NLUsername + "/";
            }
            else {
                if (this.OS.equalsIgnoreCase(MACOS))
                    return MacScpHome + this.NLUsername + "/";
                else
                    return null;
            }
        }
    }

    public NeoLoadTest(ConfigurationItem remoteScript) {
        options = new ConnectionOptions();

        ConfigurationItem NeoLoad = remoteScript.getProperty("NeoLoadPath");
        ConfigurationItem NTS = remoteScript.getProperty("NeoLoadTeamServer");
        ConfigurationItem COllab = remoteScript.getProperty("NeoLoadCollaboration");
        ConfigurationItem NLWeb=remoteScript.getProperty("NeoLoadWebAPI");
        //----setting related to NeoLoad---------------
        this.NLHost = NeoLoad.getProperty("NL_Host");
        this.NLUsername= NeoLoad.getProperty("username");
        this.NLPassword= NeoLoad.getProperty("password");
        this.NLInstallationPath=NeoLoad.getProperty("NL_Controller_Path");
        this.OS=NeoLoad.getProperty("OS");
        //-----------------------------------------------

        //-----settings on the test execution--------------
        this.NLScenarioName=remoteScript.getProperty("NeoloadScenario");
        this.NLProjectPath=remoteScript.getProperty("LocalNeoLoadProject");
        this.NLTestDescription=remoteScript.getProperty("NeoLoadTestDescription");
        //--------------------------------------------------

        //-----NL Web settings-------------------------------------
        if(NLWeb != null) {
            this.NLWEBURL=NLWeb.getProperty("NL_WEB_URL");
            this.NLWEBAPIToken=remoteScript.getProperty("NeoloadWebAPItoken");
            this.NlISNLWeb=true;
        }
        else
            this.NlISNLWeb=false;
        //---------------------------------------------------------

        //-----getting the information related to NTS--------------
        if(NTS != null)
        {
            this.NLIsNTS=true;
            this.NTSUrl=NTS.getProperty("TeamServerHost");
            this.NTSUsername=NTS.getProperty("username");
            this.NTSPassword=NTS.getProperty("password");
            this.NTSLicenseID=NTS.getProperty("licenceID");

        }
        else
            this.NLIsNTS=false;
        //---------------------------------------------------------

        //-----getting the information related to Collab--------------
        if(COllab!= null)
        {
            this.NLIsCollab=true;
            this.NLCollabUrl=COllab.getProperty("Url");
            this.NLCollabUsername=COllab.getProperty("username");
            this.NLCollabPassword=COllab.getProperty("password");
            this.NLCollabProjectName=remoteScript.getProperty("CollabProjectName");
            this.NLCollabProjectPath=remoteScript.getProperty("CollaborationProjectPath");
        }
        else
            this.NLIsCollab=false;
        //---------------------------------------------------------

    }

    private CmdLine GenerateCmdLine() throws GeneralSecurityException, UnsupportedEncodingException {

        String CMDline;
        CmdLine cmd=new CmdLine();

        CMDline=this.NLInstallationPath+"/bin";
        if(OS.equalsIgnoreCase(WINDOWS))
            CMDline+="NeoLoadCmd.exe";
        else
            CMDline+="/NeoLoadCmd";

        cmd.addRaw(CMDline);

        if(NLIsCollab)
        {
            cmd.addRaw("-checkoutProject "+ "\""+this.NLCollabProjectName+"\"");
            cmd.addRaw("-Collab "+ "\""+this.NLCollabUrl+this.NLCollabProjectPath+"\"");
            cmd.addRaw("-CollabLogin "+"\""+this.NLCollabUsername+":"+PasswordEncoder.encode(this.NLCollabPassword)+"\"");

        }

        if(NLIsNTS)
        {
            cmd.addRaw("-NTS "+"\""+this.NTSUrl+"\"" );
            cmd.addRaw("-NTSCollabPath "+"\""+this.NLCollabProjectPath+"\"");
            cmd.addRaw("-NTSLogin "+"\""+this.NTSUsername+":"+PasswordEncoder.encode(this.NTSPassword)+"\"");
            cmd.addRaw("-checkoutProject "+"\""+ this.NLCollabProjectName+"\"");
            cmd.addRaw("-publishTestResult ");
            cmd.addRaw("-leaseLicense "+"\""+this.NTSLicenseID+":"+NbVU+":"+Nbhour+"\"");
        }

        if(NlISNLWeb)
        {
            cmd.addRaw("-nlweb"+SPACE);
            cmd.addRaw("-nlwebAPIURL "+"\""+this.NLWEBURL+"\"");
            cmd.addRaw("-nlwebToken "+"\""+this.NLWEBAPIToken+"\"");
        }
        if(!NLIsCollab && !NLIsNTS) {
            cmd.addRaw("-project " + "\"" + this.NLProjectPath + "\"");

        }

        if(NLIsCloud)
        {
            cmd.addRaw("-NCPLogin \""+this.NLCloudUsername+":"+PasswordEncoder.encode(this.NLCloudPassword)+"\"");
        }

        if(IsVariableUsed)
        {
            cmd.addRaw("-variables \""+this.Variables+"\"");
        }

        if(IsCloudUsed)
        {
            //cmd.addRaw("-loadGenerators \""+GenerateLocalTempFolder()+"tmp.yaml\"");
            cmd.addRaw("-loadGenerators \""+GenerateFileTempFolder()+"tmp.yaml\"");
        }

        cmd.addRaw("-SLAJUnitMapping \"pass\"");
        cmd.addRaw("-SLAJUnitResults \""+GenerateFileTempFolder()+"junit.xml\"");

        if(this.ReleaseName!=null)
            cmd.addRaw("-description \""+this.NLTestDescription+"_"+this.ReleaseName+"_"+this.ReleaseId+"\"");
        else
            cmd.addRaw("-description \""+this.NLTestDescription+"\"");

        cmd.addRaw("-report \""+GenerateFileTempFolder()+"report.xml,"+GenerateFileTempFolder() +"report.pdf\"");
        cmd.addRaw("-launch "+"\""+this.NLScenarioName+"\"");
        cmd.addRaw("-noGUI");







        return cmd;
    }

    private byte[] getFilebyteArray(OverthereFile file) throws IOException {
        byte[] bytes;

        if(file.exists()) {
            InputStream i= file.getInputStream();
            return IOUtils.toByteArray(i);
        }
        else return null;

    }
    private void CopyFile( OverthereFile file) throws IOException
    {
        if(file.exists()) {
            InputStream is = file.getInputStream();
            OutputStream outputStream = new FileOutputStream(new File(file.getName()));

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = is.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }
    private OverthereConnection GetConnection()
    {

        OverthereConnection connection=null;
        options.set(ADDRESS, this.NLHost);
        options.set(USERNAME, this.NLUsername);
        options.set(PASSWORD, this.NLPassword);


        if (this.OS.equals(LINUX))
        {

            options.set(OPERATING_SYSTEM,UNIX);
            options.set(CONNECTION_TYPE,SCP);
            connection = Overthere.getConnection("ssh", options);

        } else if (this.OS.equals(WINDOWS))
        {
            options.set(OPERATING_SYSTEM, WINDOWS);
            options.set(CONNECTION_TYPE, TELNET);
            connection = Overthere.getConnection("cifs", options);
        } else if (this.OS.equals(MACOS))
        {
            options.set(OPERATING_SYSTEM,UNIX);
            options.set(CONNECTION_TYPE,SFTP);
            connection = Overthere.getConnection("ssh", options);
        }
        connection.setWorkingDirectory(connection.getFile(GenerateFileTempFolder()));
        return connection;
    }
    public CmdResponse LaunchProcess() {
        int rc = 0;
        //CapturingOverthereExecutionOutputHandler stdout = capturingHandler();
        //CapturingOverthereExecutionOutputHandler stderr = capturingHandler();
        String cmd;
        OverthereConnection connect;
        CmdLine script;
        OverthereProcess process;
        int exitCode=0;
        String OUTPUT="";
        String ERR="";
        try {

            connect=GetConnection();

            if(connect!=null)
            {
                script=GenerateCmdLine();
               // script=CmdLine.build(cmd);
            //    rc = connect.execute(stdout, stderr, scr
                 process = connect.startProcess(script);


                    BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getStdout()));
                    try {
                        String line;
                        while((line = stdout.readLine()) != null) {
                            OUTPUT+=line;
                        }
                    } finally {
                        stdout.close();
                    }
                    exitCode = process.waitFor();


                BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getStderr()));
                try {
                    String line;
                    while((line = stderr.readLine()) != null) {
                        ERR+=line;
                        System.out.println(OUTPUT);
                    }
                } finally {
                    stderr.close();
                }



                connect.close();
            }


        } catch (Exception e) {
            StringWriter stacktrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stacktrace, true));
            stderr.handleLine(stacktrace.toString());
            rc = 1;
        }


        return new CmdResponse(exitCode,OUTPUT,ERR);
    }

    private StringBuilder CreateRemoteFile(OverthereConnection connection ,String Content) throws IOException {
            StringBuilder result=new StringBuilder();

            OverthereFile motd = connection.getFile(GenerateFileTempFolder()+ "tmp.yaml");
            OutputStream w = motd.getOutputStream();
            try {
                result.append(Content);
                w.write(Content.getBytes());
            } finally {
                w.close();
                return result;
            }

    }

    public CmdResponse execute() {
        int rc = 0;
        CapturingOverthereExecutionOutputHandler stdout = capturingHandler();
        CapturingOverthereExecutionOutputHandler stderr = capturingHandler();
        String cmd;
        OverthereConnection connect=null;
        CmdLine script;
        OverthereFile junit;
        OverthereFile report;
        OverthereProcess process;
        CmdResponse response = null;
        StringBuilder log=null;
        StringBuilder comment=new StringBuilder();
        try {

            comment.append("Connection to remote Controller\n");
            connect=GetConnection();

            if(connect!=null) {
                comment.append("Connection Done.....\n");

                script = GenerateCmdLine();

                //script=CmdLine.build(cmd);
                OverthereFile f = connect.getWorkingDirectory();

                if(CloudYML != null || CloudYML!="-1")
                {
                    comment.append("Setting Cloud Session to the controller.....\n");
                    log=CreateRemoteFile(connect,CloudYML);
                }

                comment.append("Launching test.....\n");
                rc = connect.execute(stdout, stderr, script);
                comment.append("Test finished.....\n");

                junit = connect.getFile(GenerateFileTempFolder() + "report.dtd");
                CopyFile(junit);


                response = new CmdResponse(rc, stdout.getOutput(), stderr.getOutput());
                response.setReportdtdbytes(getFilebyteArray(junit));
                junit = connect.getFile(GenerateFileTempFolder() + "report.xml");

                if(log!=null)
                    response.addToOut(log);

                if (junit.exists())
                {
                    log = Getdata(junit.getInputStream(), response);
                    response.setReportXMLbytes(getFilebyteArray(junit));
                    junit.delete();

                    if( log != null)
                        response.addToOut(log);
                }

                junit = connect.getFile(GenerateFileTempFolder() + "report.pdf");
                if (junit.exists())
                {
                    response.setPDFbytes(getFilebyteArray(junit));
                    junit.delete();

                }


                junit = connect.getFile(GenerateFileTempFolder() + "junit.xml");
                if (junit.exists())
                {
                    GetJunitData(junit.getInputStream(), response);
                    response.setJunitxmlbytes(getFilebyteArray(junit));
                    response.rc=1;
                    junit.delete();
                }
                else
                {
                    response.addToErr("No Junit.xml found\n");
                }


                if(IsCloudUsed)
                {
                    junit = connect.getFile(GenerateFileTempFolder()+ "tmp.yaml");
                    if (junit.exists()) {
                         junit.delete();
                    }
                }
            }

            response.SetCommenrt(comment);

        } catch (Exception e) {
            if(response==null)
            {
                response=new CmdResponse();
            }
            StringWriter stacktrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stacktrace, true));
            stderr.handleLine(stacktrace.toString());
            rc = 1;
            response.addToErr(stacktrace.toString());
        }
        finally {
            if(connect!=null)
                connect.close();

            return response;
        }

        
               
           
       

    }

    public StringBuilder Getdata(InputStream input, CmdResponse res)
    {
        StringBuilder output= new StringBuilder();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        String hit;
        String responsetime;
        String error;
        try {
            // use the factory to create a documentbuilder
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc=builder.parse(input);
            hit=GetData(doc,"avg_hits/s",output);
            responsetime=GetData(doc,"avg_reqresponsetime",output);
            error=GetData(doc,"total_errors",output);

            res.addstat(responsetime,error,hit);

            
        }
     catch (Exception ex) {
            output.append(ex.getMessage());
       // ex.printStackTrace();
    }
    finally{
        return output;
    }
    }
    public StringBuilder GetJunitData(InputStream input, CmdResponse res)
    {
        StringBuilder output= new StringBuilder();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        int total;
        int failure;
        int success;
        StringBuilder out=new StringBuilder();
        StringBuilder error=new StringBuilder();
        StringBuilder comment=new StringBuilder();
        try {
            // use the factory to create a documentbuilder
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc=builder.parse(input);
            total=GetStats(doc,"/resource/testsuite/testcase");
            success=GetStats(doc,"/resource/testsuite/testcase/sucess");
            failure=GetStats(doc,"/resource/testsuite/testcase/failure");
            out.append("Results :\n");
            out.append("\t"+success + " SLAS were sucessful out of "+ total);

            comment.append("Results :\n");
            comment.append("\t"+success + " SLAS were sucessful out of "+ total);

            if(failure>0) {
                error.append(failure+" SlA were in error out of "+total+"\n");
                comment.append(failure+" SlA were in error out of "+total+"\n");
                GetError(doc, error,comment);
                res.addToErr(error);
            }

            res.addToOut(output);

        }
        catch (Exception ex) {
            res.addToErr(ex.getStackTrace().toString());
            // ex.printStackTrace();
        }
        finally {
            res.addToOut(out);
            res.addToOut(error);
            return comment;
        }
    }
    public int GetStats(Document doc,String Query) throws XPathExpressionException
    {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList)xPath.evaluate(Query, doc.getDocumentElement(), XPathConstants.NODESET);
        return nodes.getLength();
    }

    public void GetError(Document doc, StringBuilder output, StringBuilder comment) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList)xPath.evaluate("/resource/testsuite/testcase/failure", doc.getDocumentElement(), XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); ++i) {
                Node e = nodes.item(i);
                 output.append("Failure n° "+ (i+1)+"\n");
                 output.append("\t"+e.getFirstChild().getNodeValue()+"\n");
                 comment.append("Failure n° "+ (i+1)+"\n");
                 comment.append("\t"+e.getFirstChild().getNodeValue()+"\n");

            }

    }


    public String GetData(Document doc,String key, StringBuilder output) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        String result = null;
        NodeList nodes = (NodeList)xPath.evaluate("/report/summary/statistics/statistic[@name='"+key+"']", doc.getDocumentElement(), XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Element e = (Element) nodes.item(i);
            result= e.getAttribute("value");

        }
        return result;
    }

    public class CmdResponse {
        public int rc;
        public String stdout;
        public String stderr;
        public String responsetime;
        public String error;
        public String hits;

        public byte[] ReportXMLbytes;
        public byte[] Reportdtdbytes;
        public byte[] junitxmlbytes;
        public byte[] pdfbytes;
        public String comment=null;

        public CmdResponse(int rc, String stdout, String stderr)
        {
            this.rc = rc;

            this.stdout = stdout;
            this.stderr = stderr;
        }

        public CmdResponse()
        {
            this.rc=0;
            this.stdout="";
            this.stderr="";
        }
        private void SetCommenrt(StringBuilder com)
        {
            this.comment=com.toString();
        }
        public void addstat(String responsetime,String error, String his)
        {
            this.responsetime=responsetime;
            this.error=error;
            this.hits=his;
        }

        public void addToOut(StringBuilder s)
        {

            this.stdout+="\n" + s.toString();
        }
        public void addToOut(String s)
        {

            this.stdout+="\n" + s;
        }

        public void addToErr(StringBuilder s)
        {

            this.stderr+="\n" + s.toString();
            this.rc=0;
        }
        public void addToErr(String s)
        {

            this.stderr+="\n" + s;
            this.rc=1;
        }




        public void setReportXMLbytes(byte[] reportXMLbytes) {
            ReportXMLbytes = reportXMLbytes;
        }


        public void setReportdtdbytes(byte[] reportdtdbytes) {
            Reportdtdbytes = reportdtdbytes;
        }
        public void setPDFbytes(byte[] pdf)
        {
            pdfbytes=pdf;
        }

        public void setJunitxmlbytes(byte[] junitxmlbytes) {
            this.junitxmlbytes = junitxmlbytes;
        }




    }
    }
