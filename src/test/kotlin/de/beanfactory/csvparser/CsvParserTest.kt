package de.beanfactory.csvparser.de.beanfactory.csvparser

import de.beanfactory.csvparser.CsvParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.lang.RuntimeException

internal class CsvParserTest {

    private val csvData: String = """
mrBranche;mrBeschrTechn;mrStartJahr
Automotive;Kanban, Jira, Confluence;2017
"Information & Kommunikation; Telekommunikation";Netcracker TOMS, Tibco ESB, Schnittstellen zu externen Service-Anbietern (Deutsche Telekom WITA OSS, WITA ESS, WBCI, Arvato, Telefónica SPAIN, Komsa), Allgeier scanview, mobileX FSM, Canon Compart DocBridge, Kundenspezifische Software (Adressmanagement, Verfügbarkeitsprüfung, Verwaltung technischer Ressourcen),  Web Services (SOAP & JMS, XML, WSDL, XSD), SoapUI, Microsoft Office, BOC ADOIT, SparxSystems Enterprise Architect, Atlassian Jira & Confluence;2016
"Information & Kommunikation; Informationstechnologie";AGIS prima, Microsoft SQL Server, Microsoft Reporting Services, SQL Server Report Generator, Microsoft SQL Server Integration Services, Microsoft Visual Studio, Microsoft SQL Server Data Tools, Microsoft .NET, Java Spring Framework, Angular 8, Azure DevOps, Microsoft Office;2018
"Finanzen; Banken";RedHat Enterprise Linux 5 bis 7, Oracle Database 12 bis 19, Apache Tomcat 8 und 9, Apache Webserver, JBOSS 5 bis 7, OpenJDK 8 und 11, Atlassian Confluence, ServiceNow, Icinga, Microsoft Office, verschiedenste Banksysteme im B2B Bereich;2020
Güterverkehr;Microsoft Excel / PowerPoint / Word / Project, Lotus Notes, HPQC, Kunden interne Tools, ClearCase, ClearQuest;2016
Handel;HP-QC 11, MDNG (Masterdata Next Generation), ESB (Enterprise Service Bus) Tracking Tool, M.A.S.H. (Message Administration Service Hospital), UE Warenwirtschaftssystem, S.A.L.D.O. 3 (Sortiment. Altware. Lager. Disposition. Organisation.) PPM (Project & Portfolio Management System), SQL Developer, Jira, Confluence;2016
Telekommunikation;Java, Spring, JDBC/Hibernate, Oracle, Jax-WS, Apache CXF, SOAP/XML, SoapUi;2015
Versandhandel & Broadcast;Microsoft Windows 7/8.1/10, iOS, Android, Citrix XenDesktop, Citrix XenMobile, Citrix ShareFile, Symantec Endpoint Management (Altiris), Symantec Enterprise Vault for Exchange, Microsoft Project 2010, Microsoft Visio 2010, ITIL V3, Cisco Wireless Solution, Ricoh Multifunction System;2015
Telekommunikation;"Scrum
intelliJ
Protractor
GIT
Jira
Large/ small screen devices";2015
Telekommunikation;"Scrum
intelliJ
Protractor
GIT
Jira
Large/ small screen devices";2015            
"""


    @Test
    fun testParseCSV() {
        val parser = CsvParser()

        val result = parser.parse(csvData)

        Assertions.assertEquals(11, result.size)

        Assertions.assertEquals("mrBranche", result[0].fields[0].value)
        Assertions.assertEquals("mrBeschrTechn", result[0].fields[1].value)
        Assertions.assertEquals("mrStartJahr", result[0].fields[2].value)

        Assertions.assertEquals("Automotive", result[1].fields[0].value)
        Assertions.assertEquals("Kanban, Jira, Confluence", result[1].fields[1].value)
        Assertions.assertEquals("2017", result[1].fields[2].value)

        Assertions.assertEquals("Telekommunikation", result[9].fields[0].value)
        Assertions.assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT
            Jira
            Large/ small screen devices""".trimIndent(),
            result[9].fields[1].value
        )
        Assertions.assertEquals("2015", result[9].fields[2].value)

        Assertions.assertEquals("Telekommunikation", result[10].fields[0].value)
        Assertions.assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT
            Jira
            Large/ small screen devices""".trimIndent(),
            result[10].fields[1].value
        )
        Assertions.assertEquals("2015            ", result[10].fields[2].value)
    }


    private val csvTest2 = """
        Telekommunikation;"Scrum
        intelliJ
        Protractor
        GIT
        Jira
        Large/ small screen devices";2015            """.trimIndent()

    @Test
    fun testParseEndOfCSV() {
        val parser = CsvParser()

        val result = parser.parse(csvTest2)

        Assertions.assertEquals(1, result.size)

        Assertions.assertEquals("Telekommunikation", result[0].fields[0].value)
        Assertions.assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT
            Jira
            Large/ small screen devices""".trimIndent(), result[0].fields[1].value
        )
        Assertions.assertEquals("2015            ", result[0].fields[2].value)
    }

    private val csvTest3 = """
        Telekommunikation;"Scrum
        intelliJ
        Protractor
        GIT
        Jira
        Large/ small screen devices";2015            
        """.trimIndent()

    @Test
    fun testParseEndOfCSVWithLF() {
        val parser = CsvParser()

        val result = parser.parse(csvTest3)

        Assertions.assertEquals(1, result.size)

        Assertions.assertEquals("Telekommunikation", result[0].fields[0].value)
        Assertions.assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT
            Jira
            Large/ small screen devices""".trimIndent(), result[0].fields[1].value
        )
        Assertions.assertEquals("2015            ", result[0].fields[2].value)
    }

    private val csvTest4 = """
        Telekommunikation;"Scrum
        intelliJ
        Protractor
        GIT
        Jira
        Large/ small screen devices;2015            
        """.trimIndent()

    @Test
    fun testErrorOnUnbalancedQuotes() {
        val parser = CsvParser()

        try {
            parser.parse(csvTest4)
            Assertions.fail("Should have aborted with exception")
        }
        catch(e : RuntimeException) {
            Assertions.assertNull(e.message)
        }
    }

    private val csvTest5 = """
        Telekommunikation;"Scrum
        intelliJ
        Protractor
        GIT
        Jira
        Large/ small screen devices";2015            \""".trimIndent()

    @Test
    fun testErrorOnIncompleteEscape() {
        val parser = CsvParser()

        try {
            parser.parse(csvTest5)
            Assertions.fail("Should have aborted with exception")
        }
        catch(e : RuntimeException) {
            Assertions.assertNull(e.message)
        }
    }

    private val csvTest6 = """
        Telekommunikation;"Scrum
        intelliJ
        Protractor
        GIT
        Jira
        Large/ small screen devices"2015""".trimIndent()

    @Test
    fun testErrorOnMissingFieldSeparator() {
        val parser = CsvParser()

        try {
            parser.parse(csvTest6)
            Assertions.fail("Should have aborted with exception")
        }
        catch(e : RuntimeException) {
            Assertions.assertNull(e.message)
        }
    }

    private val csvTest7 = """
        Telekommunikation;"Scrum
        intelliJ
        Protractor
        GIT\"
        Jira
        Large/ small screen devices";2015""".trimIndent()

    @Test
    fun testProperlyDetectsEscapedTerminationCharacterInQuotedStrings() {
        val parser = CsvParser()

        val result = parser.parse(csvTest7)

        Assertions.assertEquals("Telekommunikation", result[0].fields[0].value)
        Assertions.assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT"
            Jira
            Large/ small screen devices""".trimIndent(),
            result[0].fields[1].value
        )
        Assertions.assertEquals("2015", result[0].fields[2].value)
    }

    private val csvTest8 = """
        Telekommunikation;Scrum\
        intelliJ\
        Protractor\
        GIT\"\
        Jira\
        Large/ small screen devices\;;2015""".trimIndent()

    @Test
    fun testProperlyDetectsEscapedTerminationCharacterInUnquotedStrings() {
        val parser = CsvParser()

        val result = parser.parse(csvTest8)

        Assertions.assertEquals("Telekommunikation", result[0].fields[0].value)
        Assertions.assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT"
            Jira
            Large/ small screen devices;""".trimIndent(),
            result[0].fields[1].value
        )
        Assertions.assertEquals("2015", result[0].fields[2].value)
    }
}