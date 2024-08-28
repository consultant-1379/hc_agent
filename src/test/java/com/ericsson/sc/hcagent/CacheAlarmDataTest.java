package com.ericsson.sc.hcagent;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ericsson.sc.fm.model.fi.FaultIndication;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.sc.hcagent.PodData.FaultIndicationStatus;

public class CacheAlarmDataTest
{
    private static final Logger log = LoggerFactory.getLogger(CacheAlarmDataTest.class);

    private CacheAlarmData cad = null;
    private final String folderPath = "severities_test1/";
    private final String filePath = "severities_test1/severities.json";

    private SeveritiesTracker severitiesTracker = null;

    private static final String BSF_DIAMETER = "eric-bsf-diameter";
    private static final String BSF_DIAMETER_POD_0 = "eric-bsf-diameter-6fb4494cc6-wrn24";
    private static final String BSF_DIAMETER_POD_1 = "eric-bsf-diameter-6fb4494cc6-zp6lp";
    private static final String BSF_DIAMETER_POD_2 = "eric-bsf-diameter-6fb4494cc6-wp6lp";
    private static final String BSF_DIAMETER_POD_3 = "eric-bsf-diameter-6fb4494cc6-wp9lp";

    private static final String BSF_MANAGER = "eric-bsf-manager";
    private static final String BSF_MANAGER_POD_0 = "eric-bsf-manager-6cf5644974-hz7ln";

    private static final String BSF_WORKER = "eric-bsf-worker";
    private static final String BSF_WORKER_POD_0 = "eric-bsf-worker-544dcc69d4-r6zbs";
    private static final String BSF_WORKER_POD_1 = "eric-bsf-worker-544dcc69d4-rmvw6";

    private static final String CM_MEDIATOR = "eric-cm-mediator";
    private static final String CM_MEDIATOR_POD_0 = "eric-cm-mediator-649cdc68b-gqkkg";
    private static final String CM_MEDIATOR_POD_1 = "eric-cm-mediator-649cdc68b-sb79g";

    private static final String CM_MEDIATOR_KEY_INIT = "eric-cm-mediator-key-init";
    private static final String CM_MEDIATOR_KEY_INIT_POD_0 = "eric-cm-mediator-key-init-572zd";
    private static final String CM_MEDIATOR_KEY_INIT_POD_1 = "eric-cm-mediator-key-init-532zd";
    private static final String CM_MEDIATOR_KEY_INIT_POD_2 = "eric-cm-mediator-key-init-512zd";
    private static final String CM_MEDIATOR_KEY_INIT_POD_3 = "eric-cm-mediator-key-init-577zd";
    private static final String CM_MEDIATOR_KEY_INIT_POD_4 = "eric-cm-mediator-key-init-533zd";

    private static final String CM_MEDIATOR_NOTIFIER = "eric-cm-mediator-notifier";
    private static final String CM_MEDIATOR_NOTIFIER_POD_0 = "eric-cm-mediator-notifier-5cbf84c4d-hrt2k";

    private static final String CM_YANG_PROVIDER = "eric-cm-yang-provider";
    private static final String CM_YANG_PROVIDER_POD_0 = "eric-cm-yang-provider-5666f467b4-mz7wf";

    private static final String CNOM_SERVER = "eric-cnom-server";
    private static final String CNOM_SERVER_POD_0 = "eric-cnom-server-6845c56f44-t46db";

    private static final String CSA_MANAGER = "eric-csa-manager";
    private static final String CSA_MANAGER_POD_0 = "eric-csa-manager-fb9ccd666-kbbqh";

    private static final String CSA_WORKER = "eric-csa-worker";
    private static final String CSA_WORKER_POD_0 = "eric-csa-worker-6fcb49b7cf-745lv";
    private static final String CSA_WORKER_POD_1 = "eric-csa-worker-6fcb49b7cf-p654t";

    private static final String CTRL_BRO = "eric-ctrl-bro";
    private static final String CTRL_BRO_POD_0 = "eric-ctrl-bro-0";

    private static final String DATA_COORDINATOR_ZK = "eric-data-coordinator-zk";
    private static final String DATA_COORDINATOR_ZK_POD_0 = "eric-data-coordinator-zk-0";
    private static final String DATA_COORDINATOR_ZK_POD_1 = "eric-data-coordinator-zk-1";
    private static final String DATA_COORDINATOR_ZK_POD_2 = "eric-data-coordinator-zk-2";
    private static final String DATA_COORDINATOR_ZK_POD_3 = "eric-data-coordinator-zk-3";
    private static final String DATA_COORDINATOR_ZK_POD_4 = "eric-data-coordinator-zk-4";
    private static final String DATA_COORDINATOR_ZK_POD_5 = "eric-data-coordinator-zk-5";

    private static final String DATA_COORDINATOR_ZK_AGENT = "eric-data-coordinator-zk-agent";
    private static final String DATA_COORDINATOR_ZK_AGENT_POD_0 = "eric-data-coordinator-zk-agent-0";

    private static final String DATA_DISTRIBUTED_COORDINATOR_ED = "eric-data-distributed-coordinator-ed";
    private static final String DATA_DISTRIBUTED_COORDINATOR_ED_POD_0 = "eric-data-distributed-coordinator-ed-0";
    private static final String DATA_DISTRIBUTED_COORDINATOR_ED_POD_1 = "eric-data-distributed-coordinator-ed-1";
    private static final String DATA_DISTRIBUTED_COORDINATOR_ED_POD_2 = "eric-data-distributed-coordinator-ed-2";
    private static final String DATA_DISTRIBUTED_COORDINATOR_ED_POD_3 = "eric-data-distributed-coordinator-ed-3";
    private static final String DATA_DISTRIBUTED_COORDINATOR_ED_POD_4 = "eric-data-distributed-coordinator-ed-4";
    private static final String DATA_DISTRIBUTED_COORDINATOR_ED_POD_5 = "eric-data-distributed-coordinator-ed-5";

    private static final String DATA_DISTRIBUTED_COORDINATOR_ED_AGENT = "eric-data-distributed-coordinator-ed-agent";
    private static final String DATA_DISTRIBUTED_COORDINATOR_ED_AGENT_POD_0 = "eric-data-distributed-coordinator-ed-agent-0";

    private static final String DATA_DISTRIBUTED_COORDINATOR_ED_SC = "eric-data-distributed-coordinator-ed-sc";
    private static final String DATA_DISTRIBUTED_COORDINATOR_ED_SC_POD_0 = "eric-data-distributed-coordinator-ed-sc-0";
    private static final String DATA_DISTRIBUTED_COORDINATOR_ED_SC_POD_1 = "eric-data-distributed-coordinator-ed-sc-1";
    private static final String DATA_DISTRIBUTED_COORDINATOR_ED_SC_POD_2 = "eric-data-distributed-coordinator-ed-sc-2";

    private static final String DATA_DOCUMENT_DATABASE_PG = "eric-data-document-database-pg";
    private static final String DATA_DOCUMENT_DATABASE_PG_POD_0 = "eric-data-document-database-pg-0";
    private static final String DATA_DOCUMENT_DATABASE_PG_POD_1 = "eric-data-document-database-pg-1";

    private static final String DATA_DOCUMENT_DATABASE_PG_BRAGENT = "eric-data-document-database-pg-bragent";
    private static final String DATA_DOCUMENT_DATABASE_PG_BRAGENT_POD_0 = "eric-data-document-database-pg-bragent-844fcfb6d8-sww4t";

    private static final String DATA_MESSAGE_BUS_KF = "eric-data-message-bus-kf";
    private static final String DATA_MESSAGE_BUS_KF_POD_0 = "eric-data-message-bus-kf-0";
    private static final String DATA_MESSAGE_BUS_KF_POD_1 = "eric-data-message-bus-kf-1";
    private static final String DATA_MESSAGE_BUS_KF_POD_2 = "eric-data-message-bus-kf-2";

    private static final String DATA_SEARCH_ENGINE_CURATOR = "eric-data-search-engine-curator";
    private static final String DATA_SEARCH_ENGINE_CURATOR_POD_0 = "eric-data-search-engine-curator-1615624920-9cjqc";
    private static final String DATA_SEARCH_ENGINE_CURATOR_POD_1 = "eric-data-search-engine-curator-1615624920-7cjqc";
    private static final String DATA_SEARCH_ENGINE_CURATOR_POD_2 = "eric-data-search-engine-curator-1615624920-6cjqc";
    private static final String DATA_SEARCH_ENGINE_CURATOR_POD_3 = "eric-data-search-engine-curator-1615624920-5cjqc";
    private static final String DATA_SEARCH_ENGINE_CURATOR_POD_4 = "eric-data-search-engine-curator-1615624920-4cjqc";

    private static final String DATA_SEARCH_ENGINE_DATA = "eric-data-search-engine-data";
    private static final String DATA_SEARCH_ENGINE_DATA_POD_0 = "eric-data-search-engine-data-0";
    private static final String DATA_SEARCH_ENGINE_DATA_POD_1 = "eric-data-search-engine-data-1";

    private static final String DATA_SEARCH_ENGINE_INGEST_TLS = "eric-data-search-engine-ingest-tls";
    private static final String DATA_SEARCH_ENGINE_INGEST_TLS_POD_0 = "eric-data-search-engine-ingest-tls-5b4556dc7d-lmww9";

    private static final String DATA_SEARCH_ENGINE_MASTER = "eric-data-search-engine-master";
    private static final String DATA_SEARCH_ENGINE_MASTER_POD_0 = "eric-data-search-engine-master-0";
    private static final String DATA_SEARCH_ENGINE_MASTER_POD_1 = "eric-data-search-engine-master-1";
    private static final String DATA_SEARCH_ENGINE_MASTER_POD_2 = "eric-data-search-engine-master-2";

    private static final String DATA_WIDE_COLUMN_DATABASE_CD = "eric-data-wide-column-database-cd";
    private static final String DATA_WIDE_COLUMN_DATABASE_CD_POD_0 = "eric-data-wide-column-database-cd-datacenter1-rack1-0";
    private static final String DATA_WIDE_COLUMN_DATABASE_CD_POD_1 = "eric-data-wide-column-database-cd-datacenter1-rack1-1";

    private static final String DATA_WIDE_COLUMN_DATABASE_CD_TLS_RESTARTER = "eric-data-wide-column-database-cd-tls-restarter";
    private static final String DATA_WIDE_COLUMN_DATABASE_CD_TLS_RESTARTER_POD_0 = "eric-data-wide-column-database-cd-tls-restarter-16156044606l54x";
    private static final String DATA_WIDE_COLUMN_DATABASE_CD_TLS_RESTARTER_POD_1 = "eric-data-wide-column-database-cd-tls-restarter-16112312606l54x";
    private static final String DATA_WIDE_COLUMN_DATABASE_CD_TLS_RESTARTER_POD_2 = "eric-data-wide-column-database-cd-tls-restarter-16113534606l54x";
    private static final String DATA_WIDE_COLUMN_DATABASE_CD_TLS_RESTARTER_POD_3 = "eric-data-wide-column-database-cd-tls-restarter-16178654606l54x";
    private static final String DATA_WIDE_COLUMN_DATABASE_CD_TLS_RESTARTER_POD_4 = "eric-data-wide-column-database-cd-tls-restarter-16156041236l54x";

    private static final String FH_ALARM_HANDLER = "eric-fh-alarm-handler";
    private static final String FH_ALARM_HANDLER_POD_0 = "eric-fh-alarm-handler-5cf445d85b-5p9gh";
    private static final String FH_ALARM_HANDLER_POD_1 = "eric-fh-alarm-handler-5cf445d85b-pswtb";

    private static final String FH_SNMP_ALARM_PROVIDER = "eric-fh-snmp-alarm-provider";
    private static final String FH_SNMP_ALARM_PROVIDER_POD_0 = "eric-fh-snmp-alarm-provider-79869d5c6-ddwdv";

    private static final String LM_COMBINED_SERVER_LICENSE_CONSUMER_HANDLER = "eric-lm-combined-server-license-consumer-handler";
    private static final String LM_COMBINED_SERVER_LICENSE_CONSUMER_HANDLER_POD_0 = "eric-lm-combined-server-license-consumer-handler-786876546gzdjc";
    private static final String LM_COMBINED_SERVER_LICENSE_CONSUMER_HANDLER_POD_1 = "eric-lm-combined-server-license-consumer-handler-786876546lh6v5";

    private static final String LM_COMBINED_SERVER_LICENSE_SERVER_CLIENT = "eric-lm-combined-server-license-server-client";
    private static final String LM_COMBINED_SERVER_LICENSE_SERVER_CLIENT_POD_0 = "eric-lm-combined-server-license-server-client-f4c77fddf-k89kw";

    private static final String LOG_SHIPPER = "eric-log-shipper";
    private static final String LOG_SHIPPER_POD_0 = "eric-log-shipper-8n92v";
    private static final String LOG_SHIPPER_POD_1 = "eric-log-shipper-hsmvd";
    private static final String LOG_SHIPPER_POD_2 = "eric-log-shipper-q6z7g";
    private static final String LOG_SHIPPER_POD_3 = "eric-log-shipper-q8cvd";
    private static final String LOG_SHIPPER_POD_4 = "eric-log-shipper-rzfn7";
    private static final String LOG_SHIPPER_POD_5 = "eric-log-shipper-t8blc";
    private static final String LOG_SHIPPER_POD_6 = "eric-log-shipper-tghzx";
    private static final String LOG_SHIPPER_POD_7 = "eric-log-shipper-w9d9c";

    private static final String LOG_TRANSFORMER = "eric-log-transformer";
    private static final String LOG_TRANSFORMER_POD_0 = "eric-log-transformer-56f8857978-jmlqg";
    private static final String LOG_TRANSFORMER_POD_1 = "eric-log-transformer-56f8857978-wsn9c";

    private static final String ODCA_DIAGNOSTIC_DATA_COLLECTOR = "eric-odca-diagnostic-data-collector";
    private static final String ODCA_DIAGNOSTIC_DATA_COLLECTOR_POD_0 = "eric-odca-diagnostic-data-collector-56d9f4f9cd-8gjzf";

    private static final String ODCA_DIAGNOSTIC_DATA_COLLECTOR_MANUAL = "eric-odca-diagnostic-data-collector-manual";
    private static final String ODCA_DIAGNOSTIC_DATA_COLLECTOR_MANUAL_POD_0 = "eric-odca-diagnostic-data-collector-manual-85649646db-sxrxk";

    private static final String PM_BULK_REPORTER = "eric-pm-bulk-reporter";
    private static final String PM_BULK_REPORTER_POD_0 = "eric-pm-bulk-reporter-7594c6d67d-j4qlr";

    private static final String PM_SERVER = "eric-pm-server";
    private static final String PM_SERVER_POD_0 = "eric-pm-server-0";

    private static final String SC_HCAGENT = "eric-sc-hcagent";
    private static final String SC_HCAGENT_POD_0 = "eric-sc-hcagent-6656f77767-wz5cb";

    private static final String SC_MANAGER = "eric-sc-manager";
    private static final String SC_MANAGER_POD_0 = "eric-sc-manager-7cd7ff489b-lnk8q";

    private static final String SC_SLF = "eric-sc-slf";
    private static final String SC_SLF_POD_0 = "eric-sc-slf-7c58ffb78c-sv5bm";
    private static final String SC_SLF_POD_1 = "eric-sc-slf-7c58ffb78c-xhnzh";

    private static final String SC_SPR_FE = "eric-sc-spr-fe";
    private static final String SC_SPR_FE_POD_0 = "eric-sc-spr-fe-55bbcf484c-8mhzf";
    private static final String SC_SPR_FE_POD_1 = "eric-sc-spr-fe-55bbcf484c-gwwzp";

    private static final String SCP_MANAGER = "eric-scp-manager";
    private static final String SCP_MANAGER_POD_0 = "eric-scp-manager-6655fb4fcf-hz6zj";

    private static final String SCP_WORKER = "eric-scp-worker";
    private static final String SCP_WORKER_POD_0 = "eric-scp-worker-dd7dfd85-kbdjd";
    private static final String SCP_WORKER_POD_1 = "eric-scp-worker-dd7dfd85-nsq7v";

    private static final String SEC_ADMIN_USER_MANAGEMENT = "eric-sec-admin-user-management";
    private static final String SEC_ADMIN_USER_MANAGEMENT_POD_0 = "eric-sec-admin-user-management-566f76fbd7-g6fb4";

    private static final String SEC_CERTM = "eric-sec-certm";
    private static final String SEC_CERTM_POD_0 = "eric-sec-certm-5fc67b9f7f-6hfzw";

    private static final String SEC_KEY_MANAGEMENT_MAIN = "eric-sec-key-management-main";
    private static final String SEC_KEY_MANAGEMENT_MAIN_POD_0 = "eric-sec-key-management-main-0";
    private static final String SEC_KEY_MANAGEMENT_MAIN_POD_1 = "eric-sec-key-management-main-1";

    private static final String SEC_LDAP_SERVER = "eric-sec-ldap-server";
    private static final String SEC_LDAP_SERVER_POD_0 = "eric-sec-ldap-server-0";

    private static final String SEC_LDAP_SERVER_PROXY = "eric-sec-ldap-server-proxy";
    private static final String SEC_LDAP_SERVER_PROXY_POD_0 = "eric-sec-ldap-server-proxy-b9ccdd4d4-2srt8";

    private static final String SEC_SIP_TLS_MAIN = "eric-sec-sip-tls-main";
    private static final String SEC_SIP_TLS_MAIN_POD_0 = "eric-sec-sip-tls-main-79c7bf6c55-d7dkk";

    private static final String SEPP_MANAGER = "eric-sepp-manager";
    private static final String SEPP_MANAGER_POD_0 = "eric-sepp-manager-85cb7bdcdf-zggl7";

    private static final String SEPP_WORKER = "eric-sepp-worker";
    private static final String SEPP_WORKER_POD_0 = "eric-sepp-worker-7dfdcbc574-469l6";
    private static final String SEPP_WORKER_POD_1 = "eric-sepp-worker-7dfdcbc574-qf8dq";

    private static final String STM_DIAMETER = "eric-stm-diameter";
    private static final String STM_DIAMETER_POD_0 = "eric-stm-diameter-845b67dcdb-dgnw2";
    private static final String STM_DIAMETER_POD_1 = "eric-stm-diameter-845b67dcdb-xgln5";

    private static final String STM_DIAMETER_CM = "eric-stm-diameter-cm";
    private static final String STM_DIAMETER_CM_POD_0 = "eric-stm-diameter-cm-bcd58dcf4-vxxdv";

//    private static final String SW_INVENTORY_MANAGER = "eric-sw-inventory-manager";
//    private static final String SW_INVENTORY_MANAGER_POD_0 = "eric-sw-inventory-manager-qx26h";
//    private static final String SW_INVENTORY_MANAGER_POD_1 = "eric-sw-inventory-manager-qx16h";
//    private static final String SW_INVENTORY_MANAGER_POD_2 = "eric-sw-inventory-manager-qx46h";
//    private static final String SW_INVENTORY_MANAGER_POD_3 = "eric-sw-inventory-manager-qx56h";
//    private static final String SW_INVENTORY_MANAGER_POD_4 = "eric-sw-inventory-manager-qx76h";
    private static final String SI_APPLICATION_SYS_INFO_HANDLER = "eric-si-application-sys-info-handler";
    private static final String SI_APPLICATION_SYS_INFO_HANDLER_POD_0 = "eric-si-application-sys-info-handler-qx26h";
    private static final String SI_APPLICATION_SYS_INFO_HANDLER_POD_1 = "eric-si-application-sys-info-handler-qx16h";
    private static final String SI_APPLICATION_SYS_INFO_HANDLER_POD_2 = "eric-si-application-sys-info-handler-qx46h";
    private static final String SI_APPLICATION_SYS_INFO_HANDLER_POD_3 = "eric-si-application-sys-info-handler-qx56h";
    private static final String SI_APPLICATION_SYS_INFO_HANDLER_POD_4 = "eric-si-application-sys-info-handler-qx76h";

    private static final String TM_INGRESS_CONTROLLER_CR_CONTOUR = "eric-tm-ingress-controller-cr-contour";
    private static final String TM_INGRESS_CONTROLLER_CR_CONTOUR_POD_0 = "eric-tm-ingress-controller-cr-contour-544755947f-s4ccr";
    private static final String TM_INGRESS_CONTROLLER_CR_CONTOUR_POD_1 = "eric-tm-ingress-controller-cr-contour-544755947f-zq2rp";

    private static final String TM_INGRESS_CONTROLLER_CR_ENVOY = "eric-tm-ingress-controller-cr-envoy";
    private static final String TM_INGRESS_CONTROLLER_CR_ENVOY_POD_0 = "eric-tm-ingress-controller-cr-envoy-2q5xp";
    private static final String TM_INGRESS_CONTROLLER_CR_ENVOY_POD_1 = "eric-tm-ingress-controller-cr-envoy-68htk";
    private static final String TM_INGRESS_CONTROLLER_CR_ENVOY_POD_2 = "eric-tm-ingress-controller-cr-envoy-7mblf";
    private static final String TM_INGRESS_CONTROLLER_CR_ENVOY_POD_3 = "eric-tm-ingress-controller-cr-envoy-7mjl6";
    private static final String TM_INGRESS_CONTROLLER_CR_ENVOY_POD_4 = "eric-tm-ingress-controller-cr-envoy-cfg8h";
    private static final String TM_INGRESS_CONTROLLER_CR_ENVOY_POD_5 = "eric-tm-ingress-controller-cr-envoy-cnlr2";
    private static final String TM_INGRESS_CONTROLLER_CR_ENVOY_POD_6 = "eric-tm-ingress-controller-cr-envoy-frn6v";
    private static final String TM_INGRESS_CONTROLLER_CR_ENVOY_POD_7 = "eric-tm-ingress-controller-cr-envoy-t6mwt";

    private static final String SC_RLF = "eric-sc-rlf";
    private static final String SC_RLF_POD_0 = "eric-sc-rlf-7c58ffb78c-sv5bm";
    private static final String SC_RLF_POD_1 = "eric-sc-rlf-7c58ffb78c-xhnzh";

    private static final String DATA_KEY_VALUE_DATABASE_RD_RLF = "eric-data-key-value-database-rd";
    private static final String DATA_KEY_VALUE_DATABASE_RD = "eric-data-key-value-database-rd";
    private static final String DATA_KEY_VALUE_DATABASE_RD_RLF_POD_1 = "eric-data-key-value-database-rd-rlf-bjh6z";
    private static final String DATA_KEY_VALUE_DATABASE_RD_RLF_POD_2 = "eric-data-key-value-database-rd-rlf-hxjzp";
    private static final String DATA_KEY_VALUE_DATABASE_RD_RLF_POD_3 = "eric-data-key-value-database-rd-rlf-jflsw";
    private static final String DATA_KEY_VALUE_DATABASE_RD_POD = "eric-data-key-value-database-rd-cd9d79f9d-gvxr4";

    private static final String FAILED = "FAILED";

    @BeforeClass
    public void beforeClass()
    {
        log.info("Setup environment prior execution of any method in this test class.");

        var res = this.getClass().getResource(this.filePath);

        assertTrue(res != null, "The file " + this.filePath + " does not exist.");

    }

    @AfterClass
    public void afterClass()
    {
        log.info("Cleanup activities after the execution of all methods in this test class.");

        log.info("Check that all controllers removed from the controller list.");
        assertTrue(this.cad.getControllers().isEmpty(), "Unable to restore environment and remove all controllers from the pod list");

//        log.info("Check that all failing pods removed from the pod list.");
//        assertTrue(this.cad.getPods().isEmpty(), "Unable to restore environment and remove all failing pods from the pod list");

        log.info("Check that alarm cache is empty.");
        assertTrue(this.cad.getAlarmCache().isEmpty(), "Unable to restore environment and remove all failing pods from alarm cache.");

        log.info("Check that alarm severity is CLEAR.");
        assertTrue(this.cad.getAlarmSeverity().equals(Severity.CLEAR), "Unable to restore environment and reset Alarm Severity to CLEAR.");
    }

    @BeforeMethod
    public void beforeMethod()
    {
        log.info("Execute specific actions/checks prior the execution of each method in this test class.");
        String severitiesPath = "";

        var faultIndication = new FaultIndication();
        faultIndication = new FaultIndicationBuilder(faultIndication).withServiceName("ericsson-sc")
                                                                     .withFaultName("POD_Failure")
                                                                     .withFaultyResource("ericsson-sc")
                                                                     .build();

        try
        {
            severitiesPath = this.getClass().getResource(this.folderPath).getPath();

            log.debug("Severities folder path: {}", severitiesPath);
        }
        catch (Exception e)
        {
            log.error(e.toString());
            assertTrue(false, "Testcase was unable to find" + this.folderPath + " folder.");
        }

        this.severitiesTracker = new SeveritiesTracker(severitiesPath);
        this.cad = new CacheAlarmData(this.severitiesTracker, faultIndication);

        log.info("Addition of SC related services with the default expected replicas.");
        this.cad.addController(SCP_MANAGER, 1);
        this.cad.addController(SCP_WORKER, 2);
        this.cad.addController(BSF_MANAGER, 1);
        this.cad.addController(BSF_WORKER, 2);
        this.cad.addController(BSF_DIAMETER, 4);
        this.cad.addController(SEPP_MANAGER, 1);
        this.cad.addController(SEPP_WORKER, 2);
        this.cad.addController(CSA_MANAGER, 1);
        this.cad.addController(CSA_WORKER, 2);
        this.cad.addController(SC_HCAGENT, 1);
        this.cad.addController(SC_MANAGER, 1);
        this.cad.addController(SC_SLF, 2);
        this.cad.addController(SC_SPR_FE, 2);
        this.cad.addController(SC_RLF, 2);

        log.info("Addition of ADP related services with the default expected replicas.");
        this.cad.addController(CM_MEDIATOR, 2);
        this.cad.addController(CM_MEDIATOR_NOTIFIER, 1);
        this.cad.addController(CM_YANG_PROVIDER, 1);
        this.cad.addController(CNOM_SERVER, 1);
        this.cad.addController(DATA_DOCUMENT_DATABASE_PG_BRAGENT, 1);
        this.cad.addController(DATA_SEARCH_ENGINE_INGEST_TLS, 1);
        this.cad.addController(FH_ALARM_HANDLER, 2);
        this.cad.addController(FH_SNMP_ALARM_PROVIDER, 1);
        this.cad.addController(LM_COMBINED_SERVER_LICENSE_CONSUMER_HANDLER, 2);
        this.cad.addController(LM_COMBINED_SERVER_LICENSE_SERVER_CLIENT, 1);
        this.cad.addController(LOG_TRANSFORMER, 2);
        this.cad.addController(ODCA_DIAGNOSTIC_DATA_COLLECTOR, 1);
        this.cad.addController(ODCA_DIAGNOSTIC_DATA_COLLECTOR_MANUAL, 1);
        this.cad.addController(PM_BULK_REPORTER, 1);
        this.cad.addController(SEC_ADMIN_USER_MANAGEMENT, 1);
        this.cad.addController(SEC_CERTM, 1);
        this.cad.addController(SEC_LDAP_SERVER_PROXY, 1);
        this.cad.addController(SEC_SIP_TLS_MAIN, 1);
        this.cad.addController(STM_DIAMETER, 2);
        this.cad.addController(STM_DIAMETER_CM, 1);
        this.cad.addController(TM_INGRESS_CONTROLLER_CR_CONTOUR, 2);
        this.cad.addController(CTRL_BRO, 1);
        this.cad.addController(DATA_COORDINATOR_ZK, 6);
        this.cad.addController(DATA_COORDINATOR_ZK_AGENT, 1);
        this.cad.addController(DATA_DISTRIBUTED_COORDINATOR_ED, 6);
        this.cad.addController(DATA_DISTRIBUTED_COORDINATOR_ED_AGENT, 1);
        this.cad.addController(DATA_DISTRIBUTED_COORDINATOR_ED_SC, 3);
        this.cad.addController(DATA_DOCUMENT_DATABASE_PG, 2);
        this.cad.addController(DATA_MESSAGE_BUS_KF, 3);
        this.cad.addController(DATA_SEARCH_ENGINE_DATA, 2);
        this.cad.addController(DATA_SEARCH_ENGINE_MASTER, 3);
        this.cad.addController(DATA_WIDE_COLUMN_DATABASE_CD, 2);
        this.cad.addController(PM_SERVER, 1);
        this.cad.addController(SEC_KEY_MANAGEMENT_MAIN, 2);
        this.cad.addController(SEC_LDAP_SERVER, 1);
        this.cad.addController(LOG_SHIPPER, 0);
        this.cad.addController(TM_INGRESS_CONTROLLER_CR_ENVOY, 0);
        this.cad.addController(DATA_SEARCH_ENGINE_CURATOR, 0);
        this.cad.addController(DATA_WIDE_COLUMN_DATABASE_CD_TLS_RESTARTER, 0);
        this.cad.addController(SI_APPLICATION_SYS_INFO_HANDLER, 0);
        this.cad.addController(CM_MEDIATOR_KEY_INIT, 0);
        this.cad.addController(DATA_KEY_VALUE_DATABASE_RD_RLF, 3);
        this.cad.addController(DATA_KEY_VALUE_DATABASE_RD, 1);
    }

    @AfterMethod
    public void afterMethod()
    {
        log.info("Execute specific actions/checks after the execution of each method in this test class.");

        log.info("Remove all controllers from controller list");
        this.cad.getControllers().clear();
        assertTrue(this.cad.getControllers().isEmpty(), "Failed to remove all controllers from the pod list");

//        log.info("Remove all failing pods from pod list.");
//        this.cad.getPods().clear();
//        assertTrue(this.cad.getPods().isEmpty(), "Failed to remove all failing pods from the pod list");

        log.info("Remove all failing pods from alarm cache.");
        this.cad.getAlarmCache().clear();
        assertTrue(this.cad.getAlarmCache().isEmpty(), "Failed to remove all failing pods from alarm cache.");

        log.info("Reset severity to CLEAR.");
        this.cad.calculateAlarmSeverity();
        assertTrue(this.cad.getAlarmSeverity().equals(Severity.CLEAR), "Failed to reset Alarm Severity to CLEAR.");
    }

    /*
     * Verify that current test class has alarm severities, controllers defined but
     * no failed pods and both alarm/waiting cache is empty.
     */
    @Test(enabled = true)
    public void tc000()
    {
        assertFalse(this.severitiesTracker.getAlarmSeverities().isEmpty(), "Imported alarm severities list is empty.");
        assertFalse(this.cad.getControllers().isEmpty(), "Expected dummy controllers are missing from the controller list.");
//        assertTrue(this.cad.getPods().isEmpty(), "Unexpectedly failed pods list is not empty.");
        assertTrue(this.cad.getAlarmCache().isEmpty(), "Unexpectedly AlarmCache is not empty.");
        assertTrue(this.cad.getAlarmSeverity().equals(FaultIndication.Severity.CLEAR),
                   "Expected CLEAR severity not present, instead " + this.cad.getAlarmSeverity() + " calculated.");

    }

    /*
     * Complete failure
     */
    @Test(enabled = true)
    public void tc001CompleteFailure()
    {
        PodData cmypPodData = new PodData();
        cmypPodData.setPodName(CM_YANG_PROVIDER_POD_0);
        cmypPodData.setPodController(CM_YANG_PROVIDER);
        cmypPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(cmypPodData);
        this.cad.calculateAlarmSeverity();

        PodData wcdbcdPodData1 = new PodData();
        wcdbcdPodData1.setPodName(DATA_WIDE_COLUMN_DATABASE_CD_POD_0);
        wcdbcdPodData1.setPodController(DATA_WIDE_COLUMN_DATABASE_CD);
        wcdbcdPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(wcdbcdPodData1);
        PodData wcdbcdPodData2 = new PodData();
        wcdbcdPodData2.setPodName(DATA_WIDE_COLUMN_DATABASE_CD_POD_1);
        wcdbcdPodData2.setPodController(DATA_WIDE_COLUMN_DATABASE_CD);
        wcdbcdPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(wcdbcdPodData2);
        this.cad.calculateAlarmSeverity();

        PodData wcdbcdTlsPodData1 = new PodData();
        wcdbcdTlsPodData1.setPodName(DATA_WIDE_COLUMN_DATABASE_CD_TLS_RESTARTER_POD_0);
        wcdbcdTlsPodData1.setPodController(DATA_WIDE_COLUMN_DATABASE_CD);
        wcdbcdTlsPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(wcdbcdTlsPodData1);
        PodData wcdbcdTlsPodData2 = new PodData();
        wcdbcdTlsPodData2.setPodName(DATA_WIDE_COLUMN_DATABASE_CD_TLS_RESTARTER_POD_1);
        wcdbcdTlsPodData2.setPodController(DATA_WIDE_COLUMN_DATABASE_CD);
        wcdbcdTlsPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(wcdbcdTlsPodData2);
        PodData wcdbcdTlsPodData3 = new PodData();
        wcdbcdTlsPodData3.setPodName(DATA_WIDE_COLUMN_DATABASE_CD_TLS_RESTARTER_POD_2);
        wcdbcdTlsPodData3.setPodController(DATA_WIDE_COLUMN_DATABASE_CD);
        wcdbcdTlsPodData3.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(wcdbcdTlsPodData3);
        PodData wcdbcdTlsPodData4 = new PodData();
        wcdbcdTlsPodData4.setPodName(DATA_WIDE_COLUMN_DATABASE_CD_TLS_RESTARTER_POD_3);
        wcdbcdTlsPodData4.setPodController(DATA_WIDE_COLUMN_DATABASE_CD);
        wcdbcdTlsPodData4.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(wcdbcdTlsPodData4);
        PodData wcdbcdTlsPodData5 = new PodData();
        wcdbcdTlsPodData5.setPodName(DATA_WIDE_COLUMN_DATABASE_CD_TLS_RESTARTER_POD_4);
        wcdbcdTlsPodData5.setPodController(DATA_WIDE_COLUMN_DATABASE_CD);
        wcdbcdTlsPodData5.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(wcdbcdTlsPodData5);
        this.cad.calculateAlarmSeverity();

        PodData snmpAlarmProviderPodData = new PodData();
        snmpAlarmProviderPodData.setPodName(FH_SNMP_ALARM_PROVIDER_POD_0);
        snmpAlarmProviderPodData.setPodController(FH_SNMP_ALARM_PROVIDER);
        snmpAlarmProviderPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(snmpAlarmProviderPodData);
        this.cad.calculateAlarmSeverity();

        PodData bsfDiameterPodData1 = new PodData();
        bsfDiameterPodData1.setPodName(BSF_DIAMETER_POD_0);
        bsfDiameterPodData1.setPodController(BSF_DIAMETER);
        bsfDiameterPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(bsfDiameterPodData1);
        PodData bsfDiameterPodData2 = new PodData();
        bsfDiameterPodData2.setPodName(BSF_DIAMETER_POD_1);
        bsfDiameterPodData2.setPodController(BSF_DIAMETER);
        bsfDiameterPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(bsfDiameterPodData2);
        PodData bsfDiameterPodData3 = new PodData();
        bsfDiameterPodData3.setPodName(BSF_DIAMETER_POD_2);
        bsfDiameterPodData3.setPodController(BSF_DIAMETER);
        bsfDiameterPodData3.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(bsfDiameterPodData3);
        PodData bsfDiameterPodData4 = new PodData();
        bsfDiameterPodData4.setPodName(BSF_DIAMETER_POD_3);
        bsfDiameterPodData4.setPodController(BSF_DIAMETER);
        bsfDiameterPodData4.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(bsfDiameterPodData4);
        this.cad.calculateAlarmSeverity();

        PodData bsfManagerPodData = new PodData();
        bsfManagerPodData.setPodName(BSF_MANAGER_POD_0);
        bsfManagerPodData.setPodController(BSF_MANAGER);
        bsfManagerPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(bsfManagerPodData);
        this.cad.calculateAlarmSeverity();

        PodData bsfWorkerPodData1 = new PodData();
        bsfWorkerPodData1.setPodName(BSF_WORKER_POD_0);
        bsfWorkerPodData1.setPodController(BSF_WORKER);
        bsfWorkerPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(bsfWorkerPodData1);
        PodData bsfWorkerPodData2 = new PodData();
        bsfWorkerPodData2.setPodName(BSF_WORKER_POD_1);
        bsfWorkerPodData2.setPodController(BSF_WORKER);
        bsfWorkerPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(bsfWorkerPodData2);
        this.cad.calculateAlarmSeverity();

        PodData cmMediatorKeyInitPodData1 = new PodData();
        cmMediatorKeyInitPodData1.setPodName(CM_MEDIATOR_KEY_INIT_POD_0);
        cmMediatorKeyInitPodData1.setPodController(CM_MEDIATOR_KEY_INIT);
        cmMediatorKeyInitPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(cmMediatorKeyInitPodData1);
        PodData cmMediatorKeyInitPodData2 = new PodData();
        cmMediatorKeyInitPodData2.setPodName(CM_MEDIATOR_KEY_INIT_POD_1);
        cmMediatorKeyInitPodData2.setPodController(CM_MEDIATOR_KEY_INIT);
        cmMediatorKeyInitPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(cmMediatorKeyInitPodData2);
        PodData cmMediatorKeyInitPodData4 = new PodData();
        cmMediatorKeyInitPodData4.setPodName(CM_MEDIATOR_KEY_INIT_POD_2);
        cmMediatorKeyInitPodData4.setPodController(CM_MEDIATOR_KEY_INIT);
        cmMediatorKeyInitPodData4.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(cmMediatorKeyInitPodData4);
        PodData cmMediatorKeyInitPodData5 = new PodData();
        cmMediatorKeyInitPodData5.setPodName(CM_MEDIATOR_KEY_INIT_POD_3);
        cmMediatorKeyInitPodData5.setPodController(CM_MEDIATOR_KEY_INIT);
        cmMediatorKeyInitPodData5.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(cmMediatorKeyInitPodData5);
        PodData cmMediatorKeyInitPodData6 = new PodData();
        cmMediatorKeyInitPodData6.setPodName(CM_MEDIATOR_KEY_INIT_POD_4);
        cmMediatorKeyInitPodData6.setPodController(CM_MEDIATOR_KEY_INIT);
        cmMediatorKeyInitPodData6.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(cmMediatorKeyInitPodData6);
        this.cad.calculateAlarmSeverity();

        PodData cmMediatorNotifierPodData = new PodData();
        cmMediatorNotifierPodData.setPodName(CM_MEDIATOR_NOTIFIER_POD_0);
        cmMediatorNotifierPodData.setPodController(CM_MEDIATOR_NOTIFIER);
        cmMediatorNotifierPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(cmMediatorNotifierPodData);
        this.cad.calculateAlarmSeverity();

        PodData cmMediatorPodData1 = new PodData();
        cmMediatorPodData1.setPodName(CM_MEDIATOR_POD_0);
        cmMediatorPodData1.setPodController(CM_MEDIATOR);
        cmMediatorPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(cmMediatorPodData1);
        PodData cmMediatorPodData2 = new PodData();
        cmMediatorPodData2.setPodName(CM_MEDIATOR_POD_1);
        cmMediatorPodData2.setPodController(CM_MEDIATOR);
        cmMediatorPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(cmMediatorPodData2);
        this.cad.calculateAlarmSeverity();

        PodData cnomServerPodData = new PodData();
        cnomServerPodData.setPodName(CNOM_SERVER_POD_0);
        cnomServerPodData.setPodController(CNOM_SERVER);
        cnomServerPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(cnomServerPodData);
        this.cad.calculateAlarmSeverity();

        PodData csaManagerPodData = new PodData();
        csaManagerPodData.setPodName(CSA_MANAGER_POD_0);
        csaManagerPodData.setPodController(CSA_MANAGER);
        csaManagerPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(csaManagerPodData);
        this.cad.calculateAlarmSeverity();

        PodData csaWorkerPodData1 = new PodData();
        csaWorkerPodData1.setPodName(CSA_WORKER_POD_0);
        csaWorkerPodData1.setPodController(CSA_WORKER);
        csaWorkerPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(csaWorkerPodData1);
        PodData csaWorkerPodData2 = new PodData();
        csaWorkerPodData2.setPodName(CSA_WORKER_POD_1);
        csaWorkerPodData2.setPodController(CSA_WORKER);
        csaWorkerPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(csaWorkerPodData2);
        this.cad.calculateAlarmSeverity();

        PodData ctrlBroPodData = new PodData();
        ctrlBroPodData.setPodName(CTRL_BRO_POD_0);
        ctrlBroPodData.setPodController(CTRL_BRO);
        ctrlBroPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(ctrlBroPodData);
        this.cad.calculateAlarmSeverity();

        PodData dataCoordinatorZooKeeperAgent = new PodData();
        dataCoordinatorZooKeeperAgent.setPodName(DATA_COORDINATOR_ZK_AGENT_POD_0);
        dataCoordinatorZooKeeperAgent.setPodController(DATA_COORDINATOR_ZK_AGENT);
        dataCoordinatorZooKeeperAgent.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dataCoordinatorZooKeeperAgent);
        this.cad.calculateAlarmSeverity();

        PodData dataCoordinatorZooKeeper1 = new PodData();
        dataCoordinatorZooKeeper1.setPodName(DATA_COORDINATOR_ZK_POD_0);
        dataCoordinatorZooKeeper1.setPodController(DATA_COORDINATOR_ZK);
        dataCoordinatorZooKeeper1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dataCoordinatorZooKeeper1);
        PodData dataCoordinatorZooKeeper2 = new PodData();
        dataCoordinatorZooKeeper2.setPodName(DATA_COORDINATOR_ZK_POD_1);
        dataCoordinatorZooKeeper2.setPodController(DATA_COORDINATOR_ZK);
        dataCoordinatorZooKeeper2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dataCoordinatorZooKeeper2);
        PodData dataCoordinatorZooKeeper3 = new PodData();
        dataCoordinatorZooKeeper3.setPodName(DATA_COORDINATOR_ZK_POD_2);
        dataCoordinatorZooKeeper3.setPodController(DATA_COORDINATOR_ZK);
        dataCoordinatorZooKeeper3.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dataCoordinatorZooKeeper3);
        PodData dataCoordinatorZooKeeper4 = new PodData();
        dataCoordinatorZooKeeper4.setPodName(DATA_COORDINATOR_ZK_POD_3);
        dataCoordinatorZooKeeper4.setPodController(DATA_COORDINATOR_ZK);
        dataCoordinatorZooKeeper4.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dataCoordinatorZooKeeper4);
        PodData dataCoordinatorZooKeeper5 = new PodData();
        dataCoordinatorZooKeeper5.setPodName(DATA_COORDINATOR_ZK_POD_4);
        dataCoordinatorZooKeeper5.setPodController(DATA_COORDINATOR_ZK);
        dataCoordinatorZooKeeper5.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dataCoordinatorZooKeeper5);
        PodData dataCoordinatorZooKeeper6 = new PodData();
        dataCoordinatorZooKeeper6.setPodName(DATA_COORDINATOR_ZK_POD_5);
        dataCoordinatorZooKeeper6.setPodController(DATA_COORDINATOR_ZK);
        dataCoordinatorZooKeeper6.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dataCoordinatorZooKeeper6);
        this.cad.calculateAlarmSeverity();

        PodData dcedPodData = new PodData();
        dcedPodData.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_AGENT_POD_0);
        dcedPodData.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED_AGENT);
        dcedPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dcedPodData);
        this.cad.calculateAlarmSeverity();

        PodData dcedPodData1 = new PodData();
        dcedPodData1.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_POD_0);
        dcedPodData1.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED);
        dcedPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dcedPodData1);
        PodData dcedPodData2 = new PodData();
        dcedPodData2.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_POD_1);
        dcedPodData2.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED);
        dcedPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dcedPodData2);
        PodData dcedPodData3 = new PodData();
        dcedPodData3.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_POD_2);
        dcedPodData3.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED);
        dcedPodData3.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dcedPodData3);
        PodData dcedPodData4 = new PodData();
        dcedPodData4.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_POD_3);
        dcedPodData4.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED);
        dcedPodData4.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dcedPodData4);
        PodData dcedPodData5 = new PodData();
        dcedPodData5.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_POD_4);
        dcedPodData5.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED);
        dcedPodData5.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dcedPodData5);
        PodData dcedPodData6 = new PodData();
        dcedPodData6.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_POD_5);
        dcedPodData6.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED);
        dcedPodData6.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dcedPodData6);
        this.cad.calculateAlarmSeverity();

        PodData dcedScPodData1 = new PodData();
        dcedScPodData1.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_SC_POD_0);
        dcedScPodData1.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED_SC);
        dcedScPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dcedScPodData1);
        PodData dcedScPodData2 = new PodData();
        dcedScPodData2.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_SC_POD_1);
        dcedScPodData2.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED_SC);
        dcedScPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dcedScPodData2);
        PodData dcedScPodData3 = new PodData();
        dcedScPodData3.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_SC_POD_2);
        dcedScPodData3.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED_SC);
        dcedScPodData3.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(dcedScPodData3);
        this.cad.calculateAlarmSeverity();

        PodData documentDatbasePgBrAgentPodData = new PodData();
        documentDatbasePgBrAgentPodData.setPodName(DATA_DOCUMENT_DATABASE_PG_BRAGENT_POD_0);
        documentDatbasePgBrAgentPodData.setPodController(DATA_DOCUMENT_DATABASE_PG_BRAGENT);
        documentDatbasePgBrAgentPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(documentDatbasePgBrAgentPodData);
        this.cad.calculateAlarmSeverity();

        PodData documentDatabasePgPodData1 = new PodData();
        documentDatabasePgPodData1.setPodName(DATA_DOCUMENT_DATABASE_PG_POD_0);
        documentDatabasePgPodData1.setPodController(DATA_DOCUMENT_DATABASE_PG);
        documentDatabasePgPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(documentDatabasePgPodData1);
        PodData documentDatabasePgPodData2 = new PodData();
        documentDatabasePgPodData2.setPodName(DATA_DOCUMENT_DATABASE_PG_POD_1);
        documentDatabasePgPodData2.setPodController(DATA_DOCUMENT_DATABASE_PG);
        documentDatabasePgPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(documentDatabasePgPodData2);
        this.cad.calculateAlarmSeverity();

        PodData mbkfPodData1 = new PodData();
        mbkfPodData1.setPodName(DATA_MESSAGE_BUS_KF_POD_0);
        mbkfPodData1.setPodController(DATA_MESSAGE_BUS_KF);
        mbkfPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(mbkfPodData1);
        PodData mbkfPodData2 = new PodData();
        mbkfPodData2.setPodName(DATA_MESSAGE_BUS_KF_POD_1);
        mbkfPodData2.setPodController(DATA_MESSAGE_BUS_KF);
        mbkfPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(mbkfPodData2);
        PodData mbkfPodData3 = new PodData();
        mbkfPodData3.setPodName(DATA_MESSAGE_BUS_KF_POD_2);
        mbkfPodData3.setPodController(DATA_MESSAGE_BUS_KF);
        mbkfPodData3.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(mbkfPodData3);
        this.cad.calculateAlarmSeverity();

        PodData searchEngineCuratorPodData1 = new PodData();
        searchEngineCuratorPodData1.setPodName(DATA_SEARCH_ENGINE_CURATOR_POD_0);
        searchEngineCuratorPodData1.setPodController(DATA_SEARCH_ENGINE_CURATOR);
        searchEngineCuratorPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(searchEngineCuratorPodData1);
        PodData searchEngineCuratorPodData2 = new PodData();
        searchEngineCuratorPodData2.setPodName(DATA_SEARCH_ENGINE_CURATOR_POD_1);
        searchEngineCuratorPodData2.setPodController(DATA_SEARCH_ENGINE_CURATOR);
        searchEngineCuratorPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(searchEngineCuratorPodData2);
        PodData searchEngineCuratorPodData3 = new PodData();
        searchEngineCuratorPodData3.setPodName(DATA_SEARCH_ENGINE_CURATOR_POD_2);
        searchEngineCuratorPodData3.setPodController(DATA_SEARCH_ENGINE_CURATOR);
        searchEngineCuratorPodData3.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(searchEngineCuratorPodData3);
        PodData searchEngineCuratorPodData4 = new PodData();
        searchEngineCuratorPodData4.setPodName(DATA_SEARCH_ENGINE_CURATOR_POD_3);
        searchEngineCuratorPodData4.setPodController(DATA_SEARCH_ENGINE_CURATOR);
        searchEngineCuratorPodData4.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(searchEngineCuratorPodData4);
        PodData searchEngineCuratorPodData5 = new PodData();
        searchEngineCuratorPodData5.setPodName(DATA_SEARCH_ENGINE_CURATOR_POD_4);
        searchEngineCuratorPodData5.setPodController(DATA_SEARCH_ENGINE_CURATOR);
        searchEngineCuratorPodData5.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(searchEngineCuratorPodData5);
        this.cad.calculateAlarmSeverity();

        PodData searchEngineDataPodData1 = new PodData();
        searchEngineDataPodData1.setPodName(DATA_SEARCH_ENGINE_DATA_POD_0);
        searchEngineDataPodData1.setPodController(DATA_SEARCH_ENGINE_DATA);
        searchEngineDataPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(searchEngineDataPodData1);
        PodData searchEngineDataPodData2 = new PodData();
        searchEngineDataPodData2.setPodName(DATA_SEARCH_ENGINE_DATA_POD_1);
        searchEngineDataPodData2.setPodController(DATA_SEARCH_ENGINE_DATA);
        searchEngineDataPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(searchEngineDataPodData2);
        this.cad.calculateAlarmSeverity();

        PodData searchEngineIngestTlsPodData = new PodData();
        searchEngineIngestTlsPodData.setPodName(DATA_SEARCH_ENGINE_INGEST_TLS_POD_0);
        searchEngineIngestTlsPodData.setPodController(DATA_SEARCH_ENGINE_INGEST_TLS);
        searchEngineIngestTlsPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(searchEngineIngestTlsPodData);
        this.cad.calculateAlarmSeverity();

        PodData searchEngineMasterPodData1 = new PodData();
        searchEngineMasterPodData1.setPodName(DATA_SEARCH_ENGINE_MASTER_POD_0);
        searchEngineMasterPodData1.setPodController(DATA_SEARCH_ENGINE_MASTER);
        searchEngineMasterPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(searchEngineMasterPodData1);
        PodData searchEngineMasterPodData2 = new PodData();
        searchEngineMasterPodData2.setPodName(DATA_SEARCH_ENGINE_MASTER_POD_1);
        searchEngineMasterPodData2.setPodController(DATA_SEARCH_ENGINE_MASTER);
        searchEngineMasterPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(searchEngineMasterPodData2);
        PodData searchEngineMasterPodData3 = new PodData();
        searchEngineMasterPodData3.setPodName(DATA_SEARCH_ENGINE_MASTER_POD_2);
        searchEngineMasterPodData3.setPodController(DATA_SEARCH_ENGINE_MASTER);
        searchEngineMasterPodData3.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(searchEngineMasterPodData3);
        this.cad.calculateAlarmSeverity();

        PodData alarmHandlerPodData1 = new PodData();
        alarmHandlerPodData1.setPodName(FH_ALARM_HANDLER_POD_0);
        alarmHandlerPodData1.setPodController(FH_ALARM_HANDLER);
        alarmHandlerPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(alarmHandlerPodData1);
        PodData alarmHandlerPodData2 = new PodData();
        alarmHandlerPodData2.setPodName(FH_ALARM_HANDLER_POD_1);
        alarmHandlerPodData2.setPodController(FH_ALARM_HANDLER);
        alarmHandlerPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(alarmHandlerPodData2);
        this.cad.calculateAlarmSeverity();

        PodData lmCombinedServerLicenseConsumerHandlerPodData1 = new PodData();
        lmCombinedServerLicenseConsumerHandlerPodData1.setPodName(LM_COMBINED_SERVER_LICENSE_CONSUMER_HANDLER_POD_0);
        lmCombinedServerLicenseConsumerHandlerPodData1.setPodController(LM_COMBINED_SERVER_LICENSE_CONSUMER_HANDLER);
        lmCombinedServerLicenseConsumerHandlerPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(lmCombinedServerLicenseConsumerHandlerPodData1);
        PodData lmCombinedServerLicenseConsumerHandlerPodData2 = new PodData();
        lmCombinedServerLicenseConsumerHandlerPodData2.setPodName(LM_COMBINED_SERVER_LICENSE_CONSUMER_HANDLER_POD_1);
        lmCombinedServerLicenseConsumerHandlerPodData2.setPodController(LM_COMBINED_SERVER_LICENSE_CONSUMER_HANDLER);
        lmCombinedServerLicenseConsumerHandlerPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(lmCombinedServerLicenseConsumerHandlerPodData2);
        this.cad.calculateAlarmSeverity();

        PodData lmCombinedServerLicenseServerClientPodData = new PodData();
        lmCombinedServerLicenseServerClientPodData.setPodName(LM_COMBINED_SERVER_LICENSE_SERVER_CLIENT_POD_0);
        lmCombinedServerLicenseServerClientPodData.setPodController(LM_COMBINED_SERVER_LICENSE_SERVER_CLIENT);
        lmCombinedServerLicenseServerClientPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(lmCombinedServerLicenseServerClientPodData);
        this.cad.calculateAlarmSeverity();

        PodData logShipperPodData1 = new PodData();
        logShipperPodData1.setPodName(LOG_SHIPPER_POD_0);
        logShipperPodData1.setPodController(LOG_SHIPPER);
        logShipperPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(logShipperPodData1);
        PodData logShipperPodData2 = new PodData();
        logShipperPodData2.setPodName(LOG_SHIPPER_POD_1);
        logShipperPodData2.setPodController(LOG_SHIPPER);
        logShipperPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(logShipperPodData2);
        PodData logShipperPodData3 = new PodData();
        logShipperPodData3.setPodName(LOG_SHIPPER_POD_2);
        logShipperPodData3.setPodController(LOG_SHIPPER);
        logShipperPodData3.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(logShipperPodData3);
        PodData logShipperPodData4 = new PodData();
        logShipperPodData4.setPodName(LOG_SHIPPER_POD_3);
        logShipperPodData4.setPodController(LOG_SHIPPER);
        logShipperPodData4.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(logShipperPodData4);
        PodData logShipperPodData5 = new PodData();
        logShipperPodData5.setPodName(LOG_SHIPPER_POD_4);
        logShipperPodData5.setPodController(LOG_SHIPPER);
        logShipperPodData5.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(logShipperPodData5);
        PodData logShipperPodData6 = new PodData();
        logShipperPodData6.setPodName(LOG_SHIPPER_POD_5);
        logShipperPodData6.setPodController(LOG_SHIPPER);
        logShipperPodData6.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(logShipperPodData6);
        PodData logShipperPodData7 = new PodData();
        logShipperPodData7.setPodName(LOG_SHIPPER_POD_6);
        logShipperPodData7.setPodController(LOG_SHIPPER);
        logShipperPodData7.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(logShipperPodData7);
        PodData logShipperPodData8 = new PodData();
        logShipperPodData8.setPodName(LOG_SHIPPER_POD_7);
        logShipperPodData8.setPodController(LOG_SHIPPER);
        logShipperPodData8.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(logShipperPodData8);
        this.cad.calculateAlarmSeverity();

        PodData logTransformerPodData1 = new PodData();
        logTransformerPodData1.setPodName(LOG_TRANSFORMER_POD_0);
        logTransformerPodData1.setPodController(LOG_TRANSFORMER);
        logTransformerPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(logTransformerPodData1);
        PodData logTransformerPodData2 = new PodData();
        logTransformerPodData2.setPodName(LOG_TRANSFORMER_POD_1);
        logTransformerPodData2.setPodController(LOG_TRANSFORMER);
        logTransformerPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(logTransformerPodData2);
        this.cad.calculateAlarmSeverity();

        PodData ddcManualPodData = new PodData();
        ddcManualPodData.setPodName(ODCA_DIAGNOSTIC_DATA_COLLECTOR_MANUAL_POD_0);
        ddcManualPodData.setPodController(ODCA_DIAGNOSTIC_DATA_COLLECTOR_MANUAL);
        ddcManualPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(ddcManualPodData);
        this.cad.calculateAlarmSeverity();

        PodData ddcPodData = new PodData();
        ddcPodData.setPodName(ODCA_DIAGNOSTIC_DATA_COLLECTOR_POD_0);
        ddcPodData.setPodController(ODCA_DIAGNOSTIC_DATA_COLLECTOR);
        ddcPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(ddcPodData);
        this.cad.calculateAlarmSeverity();

        PodData pmbrPodData = new PodData();
        pmbrPodData.setPodName(PM_BULK_REPORTER_POD_0);
        pmbrPodData.setPodController(PM_BULK_REPORTER);
        pmbrPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(pmbrPodData);
        this.cad.calculateAlarmSeverity();

        PodData pmServerPodData = new PodData();
        pmServerPodData.setPodName(PM_SERVER_POD_0);
        pmServerPodData.setPodController(PM_SERVER);
        pmServerPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(pmServerPodData);
        this.cad.calculateAlarmSeverity();

        PodData scpManagerPodData = new PodData();
        scpManagerPodData.setPodName(SCP_MANAGER_POD_0);
        scpManagerPodData.setPodController(SCP_MANAGER);
        scpManagerPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(scpManagerPodData);
        this.cad.calculateAlarmSeverity();

        PodData scpWorkerPodData1 = new PodData();
        scpWorkerPodData1.setPodName(SCP_WORKER_POD_0);
        scpWorkerPodData1.setPodController(SCP_WORKER);
        scpWorkerPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(scpWorkerPodData1);
        PodData scpWorkerPodData2 = new PodData();
        scpWorkerPodData2.setPodName(SCP_WORKER_POD_1);
        scpWorkerPodData2.setPodController(SCP_WORKER);
        scpWorkerPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(scpWorkerPodData2);
        this.cad.calculateAlarmSeverity();

        PodData hcAgentPodData = new PodData();
        hcAgentPodData.setPodName(SC_HCAGENT_POD_0);
        hcAgentPodData.setPodController(SC_HCAGENT);
        hcAgentPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(hcAgentPodData);
        this.cad.calculateAlarmSeverity();

        PodData scManagerPodData = new PodData();
        scManagerPodData.setPodName(SC_MANAGER_POD_0);
        scManagerPodData.setPodController(SC_MANAGER);
        scManagerPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(scManagerPodData);
        this.cad.calculateAlarmSeverity();

        PodData scSlfPodData1 = new PodData();
        scSlfPodData1.setPodName(SC_SLF_POD_0);
        scSlfPodData1.setPodController(SC_SLF);
        scSlfPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(scSlfPodData1);
        PodData scSlfPodData2 = new PodData();
        scSlfPodData2.setPodName(SC_SLF_POD_1);
        scSlfPodData2.setPodController(SC_SLF);
        scSlfPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(scSlfPodData2);
        this.cad.calculateAlarmSeverity();

        PodData scSprFePodData1 = new PodData();
        scSprFePodData1.setPodName(SC_SPR_FE_POD_0);
        scSprFePodData1.setPodController(SC_SPR_FE);
        scSprFePodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(scSprFePodData1);
        PodData scSprFePodData2 = new PodData();
        scSprFePodData2.setPodName(SC_SPR_FE_POD_1);
        scSprFePodData2.setPodController(SC_SPR_FE);
        scSprFePodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(scSprFePodData2);
        this.cad.calculateAlarmSeverity();

        PodData aumPodData = new PodData();
        aumPodData.setPodName(SEC_ADMIN_USER_MANAGEMENT_POD_0);
        aumPodData.setPodController(SEC_ADMIN_USER_MANAGEMENT);
        aumPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(aumPodData);
        this.cad.calculateAlarmSeverity();

        PodData certmPodData = new PodData();
        certmPodData.setPodName(SEC_CERTM_POD_0);
        certmPodData.setPodController(SEC_CERTM);
        certmPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(certmPodData);
        this.cad.calculateAlarmSeverity();

        PodData kmsPodData1 = new PodData();
        kmsPodData1.setPodName(SEC_KEY_MANAGEMENT_MAIN_POD_0);
        kmsPodData1.setPodController(SEC_KEY_MANAGEMENT_MAIN);
        kmsPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(kmsPodData1);
        PodData kmsPodData2 = new PodData();
        kmsPodData2.setPodName(SEC_KEY_MANAGEMENT_MAIN_POD_1);
        kmsPodData2.setPodController(SEC_KEY_MANAGEMENT_MAIN);
        kmsPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(kmsPodData2);
        this.cad.calculateAlarmSeverity();

        PodData ldapServerPodData = new PodData();
        ldapServerPodData.setPodName(SEC_LDAP_SERVER_POD_0);
        ldapServerPodData.setPodController(SEC_LDAP_SERVER);
        ldapServerPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(ldapServerPodData);
        this.cad.calculateAlarmSeverity();

        PodData ldapServerProxyPodData = new PodData();
        ldapServerProxyPodData.setPodName(SEC_LDAP_SERVER_PROXY_POD_0);
        ldapServerProxyPodData.setPodController(SEC_LDAP_SERVER_PROXY);
        ldapServerProxyPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(ldapServerProxyPodData);
        this.cad.calculateAlarmSeverity();

        PodData sipTlsPodData = new PodData();
        sipTlsPodData.setPodName(SEC_SIP_TLS_MAIN_POD_0);
        sipTlsPodData.setPodController(SEC_SIP_TLS_MAIN);
        sipTlsPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(sipTlsPodData);
        this.cad.calculateAlarmSeverity();

        PodData seppManagerPodData = new PodData();
        seppManagerPodData.setPodName(SEPP_MANAGER_POD_0);
        seppManagerPodData.setPodController(SEPP_MANAGER);
        seppManagerPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(seppManagerPodData);
        this.cad.calculateAlarmSeverity();

        PodData seppWorkerPodData1 = new PodData();
        seppWorkerPodData1.setPodName(SEPP_WORKER_POD_0);
        seppWorkerPodData1.setPodController(SEPP_WORKER);
        seppWorkerPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(seppWorkerPodData1);
        PodData seppWorkerPodData2 = new PodData();
        seppWorkerPodData2.setPodName(SEPP_WORKER_POD_1);
        seppWorkerPodData2.setPodController(SEPP_WORKER);
        seppWorkerPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(seppWorkerPodData2);
        this.cad.calculateAlarmSeverity();

        PodData stmDiameterCmPodData = new PodData();
        stmDiameterCmPodData.setPodName(STM_DIAMETER_CM_POD_0);
        stmDiameterCmPodData.setPodController(STM_DIAMETER_CM);
        stmDiameterCmPodData.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(stmDiameterCmPodData);
        this.cad.calculateAlarmSeverity();

        PodData stmDiameterPodData1 = new PodData();
        stmDiameterPodData1.setPodName(STM_DIAMETER_POD_0);
        stmDiameterPodData1.setPodController(STM_DIAMETER);
        stmDiameterPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(stmDiameterPodData1);
        PodData stmDiameterPodData2 = new PodData();
        stmDiameterPodData2.setPodName(STM_DIAMETER_POD_1);
        stmDiameterPodData2.setPodController(STM_DIAMETER);
        stmDiameterPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(stmDiameterPodData2);
        this.cad.calculateAlarmSeverity();

        PodData simPodData1 = new PodData();
        simPodData1.setPodName(SI_APPLICATION_SYS_INFO_HANDLER_POD_0);
        simPodData1.setPodController(SI_APPLICATION_SYS_INFO_HANDLER);
        simPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(simPodData1);
        PodData simPodData2 = new PodData();
        simPodData2.setPodName(SI_APPLICATION_SYS_INFO_HANDLER_POD_1);
        simPodData2.setPodController(SI_APPLICATION_SYS_INFO_HANDLER);
        simPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(simPodData2);
        PodData simPodData3 = new PodData();
        simPodData3.setPodName(SI_APPLICATION_SYS_INFO_HANDLER_POD_2);
        simPodData3.setPodController(SI_APPLICATION_SYS_INFO_HANDLER);
        simPodData3.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(simPodData3);
        PodData simPodData4 = new PodData();
        simPodData4.setPodName(SI_APPLICATION_SYS_INFO_HANDLER_POD_3);
        simPodData4.setPodController(SI_APPLICATION_SYS_INFO_HANDLER);
        simPodData4.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(simPodData4);
        PodData simPodData5 = new PodData();
        simPodData5.setPodName(SI_APPLICATION_SYS_INFO_HANDLER_POD_4);
        simPodData5.setPodController(SI_APPLICATION_SYS_INFO_HANDLER);
        simPodData5.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(simPodData5);
        this.cad.calculateAlarmSeverity();

        PodData iccrContourPodData1 = new PodData();
        iccrContourPodData1.setPodName(TM_INGRESS_CONTROLLER_CR_CONTOUR_POD_0);
        iccrContourPodData1.setPodController(TM_INGRESS_CONTROLLER_CR_CONTOUR);
        iccrContourPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(iccrContourPodData1);
        PodData iccrContourPodData2 = new PodData();
        iccrContourPodData2.setPodName(TM_INGRESS_CONTROLLER_CR_CONTOUR_POD_1);
        iccrContourPodData2.setPodController(TM_INGRESS_CONTROLLER_CR_CONTOUR);
        iccrContourPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(iccrContourPodData2);
        this.cad.calculateAlarmSeverity();

        PodData iccrEnvoyPodData1 = new PodData();
        iccrEnvoyPodData1.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_0);
        iccrEnvoyPodData1.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(iccrEnvoyPodData1);
        PodData iccrEnvoyPodData2 = new PodData();
        iccrEnvoyPodData2.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_1);
        iccrEnvoyPodData2.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(iccrEnvoyPodData2);
        PodData iccrEnvoyPodData3 = new PodData();
        iccrEnvoyPodData3.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_2);
        iccrEnvoyPodData3.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData3.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(iccrEnvoyPodData3);
        PodData iccrEnvoyPodData4 = new PodData();
        iccrEnvoyPodData4.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_3);
        iccrEnvoyPodData4.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData4.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(iccrEnvoyPodData4);
        PodData iccrEnvoyPodData5 = new PodData();
        iccrEnvoyPodData5.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_4);
        iccrEnvoyPodData5.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData5.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(iccrEnvoyPodData5);
        PodData iccrEnvoyPodData6 = new PodData();
        iccrEnvoyPodData6.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_5);
        iccrEnvoyPodData6.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData6.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(iccrEnvoyPodData6);
        PodData iccrEnvoyPodData7 = new PodData();
        iccrEnvoyPodData7.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_6);
        iccrEnvoyPodData7.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData7.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(iccrEnvoyPodData7);
        PodData iccrEnvoyPodData8 = new PodData();
        iccrEnvoyPodData8.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_7);
        iccrEnvoyPodData8.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData8.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(iccrEnvoyPodData8);
        this.cad.calculateAlarmSeverity();

        PodData scRlfPodData1 = new PodData();
        scRlfPodData1.setPodName(SC_RLF_POD_0);
        scRlfPodData1.setPodController(SC_RLF);
        scRlfPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(scRlfPodData1);
        PodData scRlfPodData2 = new PodData();
        scRlfPodData2.setPodName(SC_RLF_POD_1);
        scRlfPodData2.setPodController(SC_RLF);
        scRlfPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(scRlfPodData2);
        this.cad.calculateAlarmSeverity();

        PodData KVDBPodData1 = new PodData();
        KVDBPodData1.setPodName(DATA_KEY_VALUE_DATABASE_RD_RLF_POD_1);
        KVDBPodData1.setPodController(DATA_KEY_VALUE_DATABASE_RD_RLF);
        KVDBPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(KVDBPodData1);
        PodData KVDBPodData2 = new PodData();
        KVDBPodData2.setPodName(DATA_KEY_VALUE_DATABASE_RD_RLF_POD_2);
        KVDBPodData2.setPodController(DATA_KEY_VALUE_DATABASE_RD_RLF);
        KVDBPodData2.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(KVDBPodData2);
        PodData KVDBPodData3 = new PodData();
        KVDBPodData3.setPodName(DATA_KEY_VALUE_DATABASE_RD_RLF_POD_3);
        KVDBPodData3.setPodController(DATA_KEY_VALUE_DATABASE_RD_RLF);
        KVDBPodData3.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(KVDBPodData3);
        this.cad.calculateAlarmSeverity();

        PodData KVDBrdPodData1 = new PodData();
        KVDBrdPodData1.setPodName(DATA_KEY_VALUE_DATABASE_RD_POD);
        KVDBrdPodData1.setPodController(DATA_KEY_VALUE_DATABASE_RD);
        KVDBrdPodData1.setFaultIndicationStatus(FaultIndicationStatus.PENDING);
        this.cad.addInAlarmCache(KVDBrdPodData1);

        assertTrue(this.cad.getAlarmSeverity().equals(FaultIndication.Severity.CRITICAL),
                   "Expected CRITICAL severity not present, instead " + this.cad.getAlarmSeverity() + " calculated.");

    }

    /*
     * Verify that CRITICAL highest severity reported for daemon set in case that
     * one or multiple pod fail
     */
    @Test(enabled = true)
    public void tc002DaemonSetCritical()
    {
        log.info("Checking daemon set; {}", TM_INGRESS_CONTROLLER_CR_ENVOY);
        Severity highestSeverity = this.severitiesTracker.getAlarmSeverity(TM_INGRESS_CONTROLLER_CR_ENVOY).getHighestSeverity();
        log.info("{} highest severity: {}", TM_INGRESS_CONTROLLER_CR_ENVOY, highestSeverity);

        log.info("Single pod of daemon set {} failed.", TM_INGRESS_CONTROLLER_CR_ENVOY);
        PodData iccrEnvoyPodData1 = new PodData();
        iccrEnvoyPodData1.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_0);
        iccrEnvoyPodData1.setPodPhase(FAILED);
        iccrEnvoyPodData1.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData1.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData1);
        this.cad.calculateAlarmSeverity();
        Severity tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Multiple pod of daemon set {} failed.", TM_INGRESS_CONTROLLER_CR_ENVOY);
        PodData iccrEnvoyPodData2 = new PodData();
        iccrEnvoyPodData2.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_1);
        iccrEnvoyPodData2.setPodPhase(FAILED);
        iccrEnvoyPodData2.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData2.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData2);
        PodData iccrEnvoyPodData3 = new PodData();
        iccrEnvoyPodData3.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_2);
        iccrEnvoyPodData3.setPodPhase(FAILED);
        iccrEnvoyPodData3.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData3.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData3);
        PodData iccrEnvoyPodData4 = new PodData();
        iccrEnvoyPodData4.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_3);
        iccrEnvoyPodData4.setPodPhase(FAILED);
        iccrEnvoyPodData4.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData4.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData4);
        PodData iccrEnvoyPodData5 = new PodData();
        iccrEnvoyPodData5.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_4);
        iccrEnvoyPodData5.setPodPhase(FAILED);
        iccrEnvoyPodData5.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData5.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData5);
        PodData iccrEnvoyPodData6 = new PodData();
        iccrEnvoyPodData6.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_5);
        iccrEnvoyPodData6.setPodPhase(FAILED);
        iccrEnvoyPodData6.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData6.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData6);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("ALL pod of daemon set {} failed.", TM_INGRESS_CONTROLLER_CR_ENVOY);
        PodData iccrEnvoyPodData7 = new PodData();
        iccrEnvoyPodData7.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_6);
        iccrEnvoyPodData7.setPodPhase(FAILED);
        iccrEnvoyPodData7.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData7.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData7);
        PodData iccrEnvoyPodData8 = new PodData();
        iccrEnvoyPodData8.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_7);
        iccrEnvoyPodData8.setPodPhase(FAILED);
        iccrEnvoyPodData8.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData8.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData8);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Checking daemon set; {}", LOG_SHIPPER);
        highestSeverity = this.severitiesTracker.getAlarmSeverity(LOG_SHIPPER).getHighestSeverity();
        log.info("{} highest severity: {}", LOG_SHIPPER, highestSeverity);
    }

    /*
     * Verify that MINOR highest severity reported for daemon set in case that one
     * or multiple pod fail
     */
    @Test(enabled = true)
    public void tc003DaemonSetMinor()
    {
        log.info("Checking daemon set; {}", LOG_SHIPPER);
        Severity highestSeverity = this.severitiesTracker.getAlarmSeverity(LOG_SHIPPER).getHighestSeverity();
        log.info("{} highest severity: {}", LOG_SHIPPER, highestSeverity);

        log.info("Single pod of daemon set {} failed.", LOG_SHIPPER);
        PodData logShipperPodData1 = new PodData();
        logShipperPodData1.setPodName(LOG_SHIPPER_POD_0);
        logShipperPodData1.setPodPhase(FAILED);
        logShipperPodData1.setPodController(LOG_SHIPPER);
        logShipperPodData1.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData1);
        this.cad.calculateAlarmSeverity();
        Severity tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Multiple pod of daemon set {} failed.", LOG_SHIPPER);
        PodData logShipperPodData2 = new PodData();
        logShipperPodData2.setPodName(LOG_SHIPPER_POD_1);
        logShipperPodData2.setPodPhase(FAILED);
        logShipperPodData2.setPodController(LOG_SHIPPER);
        logShipperPodData2.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData2);
        PodData logShipperPodData3 = new PodData();
        logShipperPodData3.setPodName(LOG_SHIPPER_POD_2);
        logShipperPodData3.setPodPhase(FAILED);
        logShipperPodData3.setPodController(LOG_SHIPPER);
        logShipperPodData3.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData3);
        PodData logShipperPodData4 = new PodData();
        logShipperPodData4.setPodName(LOG_SHIPPER_POD_3);
        logShipperPodData4.setPodPhase(FAILED);
        logShipperPodData4.setPodController(LOG_SHIPPER);
        logShipperPodData4.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData4);
        PodData logShipperPodData5 = new PodData();
        logShipperPodData5.setPodName(LOG_SHIPPER_POD_4);
        logShipperPodData5.setPodPhase(FAILED);
        logShipperPodData5.setPodController(LOG_SHIPPER);
        logShipperPodData5.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData5);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("ALL pod of daemon set {} failed.", LOG_SHIPPER);
        PodData logShipperPodData6 = new PodData();
        logShipperPodData6.setPodName(LOG_SHIPPER_POD_5);
        logShipperPodData6.setPodPhase(FAILED);
        logShipperPodData6.setPodController(LOG_SHIPPER);
        logShipperPodData6.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData6);
        PodData logShipperPodData7 = new PodData();
        logShipperPodData7.setPodName(LOG_SHIPPER_POD_6);
        logShipperPodData7.setPodPhase(FAILED);
        logShipperPodData7.setPodController(LOG_SHIPPER);
        logShipperPodData7.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData7);
        PodData logShipperPodData8 = new PodData();
        logShipperPodData8.setPodName(LOG_SHIPPER_POD_7);
        logShipperPodData8.setPodPhase(FAILED);
        logShipperPodData8.setPodController(LOG_SHIPPER);
        logShipperPodData8.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData8);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");
    }

    /*
     * Verify that CRITICAL highest severity reported for daemon set in case that
     * one or multiple pod fail in MINOR highest severity service and then one or
     * multiple pod fail in CRITICAL highest severity service
     */
    @Test(enabled = true)
    public void tc004DaemonSetMinor2Critical()
    {

        log.info("Checking daemon set; {}", LOG_SHIPPER);
        Severity highestSeverity = this.severitiesTracker.getAlarmSeverity(LOG_SHIPPER).getHighestSeverity();
        log.info("{} highest severity: {}", LOG_SHIPPER, highestSeverity);

        log.info("Single pod of daemon set {} failed.", LOG_SHIPPER);
        PodData logShipperPodData1 = new PodData();
        logShipperPodData1.setPodName(LOG_SHIPPER_POD_0);
        logShipperPodData1.setPodPhase(FAILED);
        logShipperPodData1.setPodController(LOG_SHIPPER);
        logShipperPodData1.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData1);
        this.cad.calculateAlarmSeverity();
        Severity tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Multiple pod of daemon set {} failed.", LOG_SHIPPER);
        PodData logShipperPodData2 = new PodData();
        logShipperPodData2.setPodName(LOG_SHIPPER_POD_1);
        logShipperPodData2.setPodPhase(FAILED);
        logShipperPodData2.setPodController(LOG_SHIPPER);
        logShipperPodData2.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData2);
        PodData logShipperPodData3 = new PodData();
        logShipperPodData3.setPodName(LOG_SHIPPER_POD_2);
        logShipperPodData3.setPodPhase(FAILED);
        logShipperPodData3.setPodController(LOG_SHIPPER);
        logShipperPodData3.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData3);
        PodData logShipperPodData4 = new PodData();
        logShipperPodData4.setPodName(LOG_SHIPPER_POD_3);
        logShipperPodData4.setPodPhase(FAILED);
        logShipperPodData4.setPodController(LOG_SHIPPER);
        logShipperPodData4.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData4);
        PodData logShipperPodData5 = new PodData();
        logShipperPodData5.setPodName(LOG_SHIPPER_POD_4);
        logShipperPodData5.setPodPhase(FAILED);
        logShipperPodData5.setPodController(LOG_SHIPPER);
        logShipperPodData5.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData5);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("ALL pod of daemon set {} failed.", LOG_SHIPPER);
        PodData logShipperPodData6 = new PodData();
        logShipperPodData6.setPodName(LOG_SHIPPER_POD_5);
        logShipperPodData6.setPodPhase(FAILED);
        logShipperPodData6.setPodController(LOG_SHIPPER);
        logShipperPodData6.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData6);
        PodData logShipperPodData7 = new PodData();
        logShipperPodData7.setPodName(LOG_SHIPPER_POD_6);
        logShipperPodData7.setPodPhase(FAILED);
        logShipperPodData7.setPodController(LOG_SHIPPER);
        logShipperPodData7.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData7);
        PodData logShipperPodData8 = new PodData();
        logShipperPodData8.setPodName(LOG_SHIPPER_POD_7);
        logShipperPodData8.setPodPhase(FAILED);
        logShipperPodData8.setPodController(LOG_SHIPPER);
        logShipperPodData8.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData8);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Checking daemon set; {}", TM_INGRESS_CONTROLLER_CR_ENVOY);
        highestSeverity = this.severitiesTracker.getAlarmSeverity(TM_INGRESS_CONTROLLER_CR_ENVOY).getHighestSeverity();
        log.info("{} highest severity: {}", TM_INGRESS_CONTROLLER_CR_ENVOY, highestSeverity);

        log.info("Single pod of daemon set {} failed.", TM_INGRESS_CONTROLLER_CR_ENVOY);
        PodData iccrEnvoyPodData1 = new PodData();
        iccrEnvoyPodData1.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_0);
        iccrEnvoyPodData1.setPodPhase(FAILED);
        iccrEnvoyPodData1.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData1.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData1);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Multiple pod of daemon set {} failed.", TM_INGRESS_CONTROLLER_CR_ENVOY);
        PodData iccrEnvoyPodData2 = new PodData();
        iccrEnvoyPodData2.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_1);
        iccrEnvoyPodData2.setPodPhase(FAILED);
        iccrEnvoyPodData2.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData2.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData2);
        PodData iccrEnvoyPodData3 = new PodData();
        iccrEnvoyPodData3.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_2);
        iccrEnvoyPodData3.setPodPhase(FAILED);
        iccrEnvoyPodData3.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData3.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData3);
        PodData iccrEnvoyPodData4 = new PodData();
        iccrEnvoyPodData4.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_3);
        iccrEnvoyPodData4.setPodPhase(FAILED);
        iccrEnvoyPodData4.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData4.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData4);
        PodData iccrEnvoyPodData5 = new PodData();
        iccrEnvoyPodData5.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_4);
        iccrEnvoyPodData5.setPodPhase(FAILED);
        iccrEnvoyPodData5.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData5.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData5);
        PodData iccrEnvoyPodData6 = new PodData();
        iccrEnvoyPodData6.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_5);
        iccrEnvoyPodData6.setPodPhase(FAILED);
        iccrEnvoyPodData6.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData6.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData6);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("ALL pod of daemon set {} failed.", TM_INGRESS_CONTROLLER_CR_ENVOY);
        PodData iccrEnvoyPodData7 = new PodData();
        iccrEnvoyPodData7.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_6);
        iccrEnvoyPodData7.setPodPhase(FAILED);
        iccrEnvoyPodData7.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData7.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData7);
        PodData iccrEnvoyPodData8 = new PodData();
        iccrEnvoyPodData8.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_7);
        iccrEnvoyPodData8.setPodPhase(FAILED);
        iccrEnvoyPodData8.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData8.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData8);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Checking daemon set; {}", LOG_SHIPPER);
        highestSeverity = this.severitiesTracker.getAlarmSeverity(LOG_SHIPPER).getHighestSeverity();
        log.info("{} highest severity: {}", LOG_SHIPPER, highestSeverity);
    }

    /*
     * Verify that CRITICAL highest severity reported for daemon set in case that
     * one or multiple pod fail in CRITICAL highest severity service and then one or
     * multiple pod fail in MINOR highest severity service
     */
    @Test(enabled = true)
    public void tc005DaemonSetCritical2MinorStayCritical()
    {
        log.info("Checking daemon set; {}", TM_INGRESS_CONTROLLER_CR_ENVOY);
        Severity highestSeverity = this.severitiesTracker.getAlarmSeverity(TM_INGRESS_CONTROLLER_CR_ENVOY).getHighestSeverity();
        log.info("{} highest severity: {}", TM_INGRESS_CONTROLLER_CR_ENVOY, highestSeverity);

        log.info("Single pod of daemon set {} failed.", TM_INGRESS_CONTROLLER_CR_ENVOY);
        PodData iccrEnvoyPodData1 = new PodData();
        iccrEnvoyPodData1.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_0);
        iccrEnvoyPodData1.setPodPhase(FAILED);
        iccrEnvoyPodData1.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData1.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData1);
        this.cad.calculateAlarmSeverity();
        Severity tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Multiple pod of daemon set {} failed.", TM_INGRESS_CONTROLLER_CR_ENVOY);
        PodData iccrEnvoyPodData2 = new PodData();
        iccrEnvoyPodData2.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_1);
        iccrEnvoyPodData2.setPodPhase(FAILED);
        iccrEnvoyPodData2.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData2.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData2);
        PodData iccrEnvoyPodData3 = new PodData();
        iccrEnvoyPodData3.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_2);
        iccrEnvoyPodData3.setPodPhase(FAILED);
        iccrEnvoyPodData3.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData3.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData3);
        PodData iccrEnvoyPodData4 = new PodData();
        iccrEnvoyPodData4.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_3);
        iccrEnvoyPodData4.setPodPhase(FAILED);
        iccrEnvoyPodData4.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData4.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData4);
        PodData iccrEnvoyPodData5 = new PodData();
        iccrEnvoyPodData5.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_4);
        iccrEnvoyPodData5.setPodPhase(FAILED);
        iccrEnvoyPodData5.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData5.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData5);
        PodData iccrEnvoyPodData6 = new PodData();
        iccrEnvoyPodData6.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_5);
        iccrEnvoyPodData6.setPodPhase(FAILED);
        iccrEnvoyPodData6.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData6.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData6);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("ALL pod of daemon set {} failed.", TM_INGRESS_CONTROLLER_CR_ENVOY);
        PodData iccrEnvoyPodData7 = new PodData();
        iccrEnvoyPodData7.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_6);
        iccrEnvoyPodData7.setPodPhase(FAILED);
        iccrEnvoyPodData7.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData7.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData7);
        PodData iccrEnvoyPodData8 = new PodData();
        iccrEnvoyPodData8.setPodName(TM_INGRESS_CONTROLLER_CR_ENVOY_POD_7);
        iccrEnvoyPodData8.setPodPhase(FAILED);
        iccrEnvoyPodData8.setPodController(TM_INGRESS_CONTROLLER_CR_ENVOY);
        iccrEnvoyPodData8.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(iccrEnvoyPodData8);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Checking daemon set; {}", LOG_SHIPPER);
        Severity highestSeverity2 = this.severitiesTracker.getAlarmSeverity(LOG_SHIPPER).getHighestSeverity();
        log.info("{} highest severity: {}", LOG_SHIPPER, highestSeverity2);

        log.info("Checking daemon set; {}", LOG_SHIPPER);
        highestSeverity2 = this.severitiesTracker.getAlarmSeverity(LOG_SHIPPER).getHighestSeverity();
        log.info("{} highest severity: {}", LOG_SHIPPER, highestSeverity2);

        log.info("Single pod of daemon set {} failed.", LOG_SHIPPER);
        PodData logShipperPodData1 = new PodData();
        logShipperPodData1.setPodName(LOG_SHIPPER_POD_0);
        logShipperPodData1.setPodPhase(FAILED);
        logShipperPodData1.setPodController(LOG_SHIPPER);
        logShipperPodData1.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData1);
        this.cad.calculateAlarmSeverity();
        Severity tmpSeverity2 = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity2);
        assertFalse(tmpSeverity2.equals(highestSeverity2), "Unexpected " + highestSeverity2 + " severity reported, instead " + tmpSeverity2 + " calculated.");
        assertTrue(tmpSeverity2.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity2 + " calculated.");

        log.info("Multiple pod of daemon set {} failed.", LOG_SHIPPER);
        PodData logShipperPodData2 = new PodData();
        logShipperPodData2.setPodName(LOG_SHIPPER_POD_1);
        logShipperPodData2.setPodPhase(FAILED);
        logShipperPodData2.setPodController(LOG_SHIPPER);
        logShipperPodData2.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData2);
        PodData logShipperPodData3 = new PodData();
        logShipperPodData3.setPodName(LOG_SHIPPER_POD_2);
        logShipperPodData3.setPodPhase(FAILED);
        logShipperPodData3.setPodController(LOG_SHIPPER);
        logShipperPodData3.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData3);
        PodData logShipperPodData4 = new PodData();
        logShipperPodData4.setPodName(LOG_SHIPPER_POD_3);
        logShipperPodData4.setPodPhase(FAILED);
        logShipperPodData4.setPodController(LOG_SHIPPER);
        logShipperPodData4.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData4);
        PodData logShipperPodData5 = new PodData();
        logShipperPodData5.setPodName(LOG_SHIPPER_POD_4);
        logShipperPodData5.setPodPhase(FAILED);
        logShipperPodData5.setPodController(LOG_SHIPPER);
        logShipperPodData5.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData5);
        this.cad.calculateAlarmSeverity();
        tmpSeverity2 = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity2);
        assertFalse(tmpSeverity2.equals(highestSeverity2), "Unexpected " + highestSeverity2 + " severity reported, instead " + tmpSeverity2 + " calculated.");
        assertTrue(tmpSeverity2.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity2 + " calculated.");

        log.info("ALL pod of daemon set {} failed.", LOG_SHIPPER);
        PodData logShipperPodData6 = new PodData();
        logShipperPodData6.setPodName(LOG_SHIPPER_POD_5);
        logShipperPodData6.setPodPhase(FAILED);
        logShipperPodData6.setPodController(LOG_SHIPPER);
        logShipperPodData6.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData6);
        PodData logShipperPodData7 = new PodData();
        logShipperPodData7.setPodName(LOG_SHIPPER_POD_6);
        logShipperPodData7.setPodPhase(FAILED);
        logShipperPodData7.setPodController(LOG_SHIPPER);
        logShipperPodData7.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData7);
        PodData logShipperPodData8 = new PodData();
        logShipperPodData8.setPodName(LOG_SHIPPER_POD_7);
        logShipperPodData8.setPodPhase(FAILED);
        logShipperPodData8.setPodController(LOG_SHIPPER);
        logShipperPodData8.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(logShipperPodData8);
        this.cad.calculateAlarmSeverity();
        tmpSeverity2 = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity2);
        assertFalse(tmpSeverity2.equals(highestSeverity2), "Unexpected " + highestSeverity2 + " severity reported, instead " + tmpSeverity2 + " calculated.");
        assertTrue(tmpSeverity2.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity2 + " calculated.");
    }

    /**
     * Get previous severity based on current severity input
     * 
     * @param severity current severity input
     * @return
     */
    private Severity getPrevious(Severity severity)
    {
        switch (severity)
        {
            case CRITICAL:
                return Severity.MAJOR;
            case MAJOR:
                return Severity.MINOR;
            default:
                return Severity.WARNING;
        }

    }

    /*
     * Verify that thresholds work in deployments with highest severity CRITICAL and
     * based on the number of failed pods different severity reported
     */
    @Test(enabled = true)
    public void tc008DeploymentCritical()
    {
        log.info("Checking deployment; {}", BSF_DIAMETER);
        var highestSeverity = this.severitiesTracker.getAlarmSeverity(BSF_DIAMETER).getHighestSeverity();
        var highAvailabilitySeverity = this.getPrevious(highestSeverity);
        highAvailabilitySeverity = highAvailabilitySeverity.compareTo(Severity.MINOR) <= 0 ? Severity.MINOR : highAvailabilitySeverity;
        var instabilitiesSeverity = this.getPrevious(highAvailabilitySeverity);
        instabilitiesSeverity = instabilitiesSeverity.compareTo(Severity.MINOR) <= 0 ? Severity.MINOR : instabilitiesSeverity;

        log.info("{} highest severity: {}, high-availability severity: {}, instabilities severity: {}",
                 BSF_DIAMETER,
                 highestSeverity,
                 highAvailabilitySeverity,
                 instabilitiesSeverity);
        assertTrue(!highAvailabilitySeverity.equals(instabilitiesSeverity), "Unexpectadly high-availability severity and instabilities severity are equal");

        log.info("Single pod of deployment {} failed.", BSF_DIAMETER);
        PodData bsfDiameterPodData1 = new PodData();
        bsfDiameterPodData1.setPodName(BSF_DIAMETER_POD_0);
        bsfDiameterPodData1.setPodPhase(FAILED);
        bsfDiameterPodData1.setPodController(BSF_DIAMETER);
        bsfDiameterPodData1.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(bsfDiameterPodData1);
        this.cad.calculateAlarmSeverity();
        Severity tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(instabilitiesSeverity),
                   "Expected " + instabilitiesSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Multiple pods of deployment {} failed that have no effect of service high availability.", BSF_DIAMETER);
        PodData bsfDiameterPodData2 = new PodData();
        bsfDiameterPodData2.setPodName(BSF_DIAMETER_POD_1);
        bsfDiameterPodData2.setPodPhase(FAILED);
        bsfDiameterPodData2.setPodController(BSF_DIAMETER);
        bsfDiameterPodData2.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(bsfDiameterPodData2);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(instabilitiesSeverity),
                   "Expected " + instabilitiesSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Multiple pods of deployment {} failed that affect of service high availability.", BSF_DIAMETER);
        PodData bsfDiameterPodData3 = new PodData();
        bsfDiameterPodData3.setPodName(BSF_DIAMETER_POD_2);
        bsfDiameterPodData3.setPodPhase(FAILED);
        bsfDiameterPodData3.setPodController(BSF_DIAMETER);
        bsfDiameterPodData3.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(bsfDiameterPodData3);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highAvailabilitySeverity),
                   "Expected " + highAvailabilitySeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("ALL pods of deployment {} failed.", BSF_DIAMETER);
        PodData bsfDiameterPodData4 = new PodData();
        bsfDiameterPodData4.setPodName(BSF_DIAMETER_POD_3);
        bsfDiameterPodData4.setPodPhase(FAILED);
        bsfDiameterPodData4.setPodController(BSF_DIAMETER);
        bsfDiameterPodData4.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(bsfDiameterPodData4);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");
    }

    /*
     * Verify that thresholds work in deployments with highest severity MAJOR and
     * based on the number of failed pods different severity reported
     */
    @Test(enabled = true)
    public void tc009DeploymentMajor()
    {
        log.info("Checking deployment; {}", CM_YANG_PROVIDER);
        var highestSeverity = this.severitiesTracker.getAlarmSeverity(CM_YANG_PROVIDER).getHighestSeverity();
        var highAvailabilitySeverity = this.getPrevious(highestSeverity);
        highAvailabilitySeverity = highAvailabilitySeverity.compareTo(Severity.MINOR) <= 0 ? Severity.MINOR : highAvailabilitySeverity;
        var instabilitiesSeverity = this.getPrevious(highAvailabilitySeverity);
        instabilitiesSeverity = instabilitiesSeverity.compareTo(Severity.MINOR) <= 0 ? Severity.MINOR : instabilitiesSeverity;

        log.info("{} highest severity: {}, high-availability severity: {}, instabilities severity: {}",
                 CM_YANG_PROVIDER,
                 highestSeverity,
                 highAvailabilitySeverity,
                 instabilitiesSeverity);
        assertTrue(highAvailabilitySeverity.equals(instabilitiesSeverity), "Unexpectadly high-availability severity and instabilities severity are not equal");

        log.info("ALL pods of deployment {} failed.", CM_YANG_PROVIDER);
        PodData cmypPodData = new PodData();
        cmypPodData.setPodName(CM_YANG_PROVIDER_POD_0);
        cmypPodData.setPodPhase(FAILED);
        cmypPodData.setPodController(CM_YANG_PROVIDER);
        cmypPodData.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(cmypPodData);
        this.cad.calculateAlarmSeverity();
        Severity tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");
    }

    /*
     * Verify that thresholds work in statefulset and based on the number of failed
     * pods different severity reported
     */
    @Test(enabled = true)
    public void tc011StatefulSetCritical()
    {

        log.info("Checking statefulset; {}", DATA_DISTRIBUTED_COORDINATOR_ED);
        var highestSeverity = this.severitiesTracker.getAlarmSeverity(DATA_DISTRIBUTED_COORDINATOR_ED).getHighestSeverity();
        var highAvailabilitySeverity = this.getPrevious(highestSeverity);
        highAvailabilitySeverity = highAvailabilitySeverity.compareTo(Severity.MINOR) <= 0 ? Severity.MINOR : highAvailabilitySeverity;
        var instabilitiesSeverity = this.getPrevious(highAvailabilitySeverity);
        instabilitiesSeverity = instabilitiesSeverity.compareTo(Severity.MINOR) <= 0 ? Severity.MINOR : instabilitiesSeverity;

        log.info("{} highest severity: {}, high-availability severity: {}, instabilities severity: {}",
                 DATA_DISTRIBUTED_COORDINATOR_ED,
                 highestSeverity,
                 highAvailabilitySeverity,
                 instabilitiesSeverity);
        assertTrue(!highAvailabilitySeverity.equals(instabilitiesSeverity), "Unexpectadly high-availability severity and instabilities severity are equal");

        log.info("Single pod of deployment {} failed.", DATA_DISTRIBUTED_COORDINATOR_ED);
        PodData dcedPodData1 = new PodData();
        dcedPodData1.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_POD_0);
        dcedPodData1.setPodPhase(FAILED);
        dcedPodData1.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED);
        dcedPodData1.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(dcedPodData1);
        this.cad.calculateAlarmSeverity();
        Severity tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(instabilitiesSeverity),
                   "Expected " + instabilitiesSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Multiple pods of deployment {} failed that have no effect of service high availability.", DATA_DISTRIBUTED_COORDINATOR_ED);
        PodData dcedPodData2 = new PodData();
        dcedPodData2.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_POD_1);
        dcedPodData2.setPodPhase(FAILED);
        dcedPodData2.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED);
        dcedPodData2.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(dcedPodData2);
        PodData dcedPodData3 = new PodData();
        dcedPodData3.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_POD_2);
        dcedPodData3.setPodPhase(FAILED);
        dcedPodData3.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED);
        dcedPodData3.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(dcedPodData3);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(instabilitiesSeverity),
                   "Expected " + instabilitiesSeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("Multiple pods of deployment {} failed that affect of service high availability.", DATA_DISTRIBUTED_COORDINATOR_ED);
        PodData dcedPodData4 = new PodData();
        dcedPodData4.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_POD_3);
        dcedPodData4.setPodPhase(FAILED);
        dcedPodData4.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED);
        dcedPodData4.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(dcedPodData4);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highAvailabilitySeverity),
                   "Expected " + highAvailabilitySeverity + " severity not present, instead " + tmpSeverity + " calculated.");

        log.info("ALL pods of deployment {} failed.", DATA_DISTRIBUTED_COORDINATOR_ED);
        PodData dcedPodData5 = new PodData();
        dcedPodData5.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_POD_4);
        dcedPodData5.setPodPhase(FAILED);
        dcedPodData5.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED);
        dcedPodData5.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(dcedPodData5);
        PodData dcedPodData6 = new PodData();
        dcedPodData6.setPodName(DATA_DISTRIBUTED_COORDINATOR_ED_POD_5);
        dcedPodData6.setPodPhase(FAILED);
        dcedPodData6.setPodController(DATA_DISTRIBUTED_COORDINATOR_ED);
        dcedPodData6.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cad.addInAlarmCache(dcedPodData6);
        this.cad.calculateAlarmSeverity();
        tmpSeverity = this.cad.getAlarmSeverity();
        log.info("Calculated severity: {}", tmpSeverity);
        assertTrue(tmpSeverity.equals(highestSeverity), "Expected " + highestSeverity + " severity not present, instead " + tmpSeverity + " calculated.");
    }
}
