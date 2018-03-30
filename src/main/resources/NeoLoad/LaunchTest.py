import sys
from com.neotys.xebialabs.xl import NeoLoadTest

releaseid=getCurrentRelease().id
releasename=getCurrentRelease().name


if NeoLoadPath is None:
    print "No Controller defined"
    sys.exit(1)

CreateVar=False
NeoLoadCloudcreated=False
variables=releaseApi.getVariables(releaseid)
for var in variables :
    if var.key == 'NeoLoadVariable':
        NeoLoadVariable=var.value
        CreateVar=True
    if var.key== 'NeoLoadCloudYML':
        NeoLoadCloudYML=var.value
        NeoLoadCloudcreated=True
#--------add trhe release id and title
# ->${release.id}
# ->${release.title}
session = NeoLoadTest(NeoLoadPath["NL_Host"],NeoLoadPath["username"],NeoLoadPath["password"],NeoLoadPath["NL_Controller_Path"],NeoLoadPath["OS"],NeoloadScenario,LocalNeoLoadProject,NeoLoadTestDescription,NeoloadWebAPItoken,CollabProjectName,CollaborationProjectPath,NumberOfUser,NumberOfhour)
session.SetReleaseInformation(releaseid,releasename)

if NeoLoadWebAPI is not None:
    session.SetWebAPI(NeoLoadWebAPI["NL_WEB_URL"])

if NeoLoadTeamServer is not None:
    session.SetNTS(NeoLoadTeamServer["TeamServerHost"],NeoLoadTeamServer["username"],NeoLoadTeamServer["password"],NeoLoadTeamServer["licenceID"])

if NeoLoadCollaboration is not None:
    session.SetCollab(NeoLoadCollaboration["Url"],NeoLoadCollaboration["username"],NeoLoadCollaboration["password"])

if NeoloadCloudPlatform is not None:
    session.SetNLCloud(NeoloadCloudPlatform["username"],NeoloadCloudPlatform["password"])

if NeoLoadCloudcreated:
    session.AddCloudYML(NeoLoadCloudYML)

if CreateVar:
    session.SetVarialbe(NeoLoadVariable)


response = session.execute()

# set variables
output = response.stdout
error = response.stderr
NeoLoadAverageResponsetime=Variable('NeoLoadAverageResponsetime', response.responsetime)
NeoLoadAverageerror=Variable('NeoLoadAverageerror',response.error)
NeoLoadAveragehit=Variable('NeoLoadAveragehit',response.hits)
#variables=releaseApi.getVariables(releaseid)
Createaveragersp=True
Createaveragehit=True
Createaveragezerror=True


for vars in variables :
    if vars.key== 'NeoLoadAverageResponsetime':
        vars.value=response.responsetime
        releaseApi.updateVariable(vars)
        Createaveragersp=False
    if vars.key== 'NeoLoadAverageerror':
        vars.value=response.error
        releaseApi.updateVariable(vars)
        Createaveragezerror=False
    if vars.key== 'NeoLoadAveragehit':
        vars.value=response.hits
        releaseApi.updateVariable(vars)
        Createaveragehit=False

if Createaveragersp== True:
    releaseApi.createVariable(releaseid, NeoLoadAverageResponsetime)

if Createaveragehit== True:
    releaseApi.createVariable(releaseid,NeoLoadAveragehit )

if Createaveragezerror== True:
    releaseApi.createVariable(releaseid,NeoLoadAverageerror )

reportxmlbyte=response.ReportXMLbytes
reportdtdbyte=response.Reportdtdbytes
junitbyte=response.junitxmlbytes
pdfbytes=response.pdfbytes
com=response.comment
taskid=getCurrentTask().id

if reportdtdbyte is not None:
    att=taskApi.addAttachment(taskid,"report.dtd",reportdtdbyte)

if reportxmlbyte is not None:
    att=taskApi.addAttachment(taskid,"report.xml",reportxmlbyte)

if junitbyte is not None:
    att=taskApi.addAttachment(taskid,"junit.xml",junitbyte)

if pdfbytes is not None:
    att=taskApi.addAttachment(taskid,"report.pdf", pdfbytes)

if com is not None:
    comment=Comment(com)
    att=taskApi.commentTask(taskid,comment)

if response.rc == 0:
    print output
else:
    print "Exit code "
    print response.rc
    print
    print "#### Output:"
    print output

    print "#### Error stream:"
    print error
    print
    print "----"

    sys.exit(response.rc)