locals {
  app_full_name = "${var.product}-${var.component}"
}

module "snl-events" {
  source               = "git@github.com:hmcts/moj-module-webapp"
  product              = "${var.product}-${var.component}"
  location             = "${var.location}"
  env                  = "${var.env}"
  ilbIp                = "${var.ilbIp}"
  is_frontend          = false
  subscription         = "${var.subscription}"
  additional_host_name = "${var.external_host_name}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  common_tags          = "${var.common_tags}"

  app_settings = {
    SNL_EVENTS_DB_HOST = "${module.postgres-snl-events.host_name}"
    SNL_EVENTS_DB_PORT = "${module.postgres-snl-events.postgresql_listen_port}"
    SNL_EVENTS_DB_NAME = "${module.postgres-snl-events.postgresql_database}"
    SNL_EVENTS_DB_USERNAME = "${module.postgres-snl-events.user_name}"
    SNL_EVENTS_DB_PASSWORD = "${module.postgres-snl-events.postgresql_password}"
    SNL_EVENTS_DB_PARAMS = "?ssl=true"

    ENABLE_DB_MIGRATE_IN_SERVICE = "false"

    SNL_RULES_URL = "http://snl-rules-${var.env}.service.${data.terraform_remote_state.core_apps_compute.ase_name[0]}.internal"
  }

}

module "postgres-snl-events" {
  source              = "git@github.com:hmcts/moj-module-postgres?ref=master"
  product             = "${var.product}-${var.component}"
  env                 = "${var.env}"
  location            = "${var.location}"
  postgresql_user     = "${var.db_user}"
  database_name       = "${var.db_name}"
  postgresql_version  = "10"
  common_tags         = "${var.common_tags}"
}

module "snl-vault" {
  source = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name = "snl-${var.env}"
  product = "${var.product}"
  env = "${var.env}"
  tenant_id = "${var.tenant_id}"
  object_id = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  product_group_object_id = "68839600-92da-4862-bb24-1259814d1384"
}

////////////////////////////////
// Populate Vault with DB info
////////////////////////////////
/*
resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name = "${local.app_full_name}-POSTGRES-USER"
  value = "${module.user-profile-db.user_name}"
  vault_uri = "${module.user-profile-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name = "${local.app_full_name}-POSTGRES-PASS"
  value = "${module.user-profile-db.postgresql_password}"
  vault_uri = "${module.user-profile-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name = "${local.app_full_name}-POSTGRES-HOST"
  value = "${module.user-profile-db.host_name}"
  vault_uri = "${module.user-profile-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name = "${local.app_full_name}-POSTGRES-PORT"
  value = "${module.user-profile-db.postgresql_listen_port}"
  vault_uri = "${module.user-profile-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name = "${local.app_full_name}-POSTGRES-DATABASE"
  value = "${module.user-profile-db.postgresql_database}"
  vault_uri = "${module.user-profile-vault.key_vault_uri}"
}
*/
