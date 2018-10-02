locals {
  app_full_name = "${var.product}-${var.component}"

  // Specifies the type of environment. var.env is replaced by pipline
  // to i.e. pr-102-snl so then we need just aat used here
  envInUse = "${(var.env == "preview" || var.env == "spreview") ? "aat" : var.env}"

  aat_rules_url = "http://snl-rules-aat.service.core-compute-aat.internal"
  local_rules_url = "http://snl-rules-${var.env}.service.${data.terraform_remote_state.core_apps_compute.ase_name[0]}.internal"
  rules_url = "${var.env == "preview" ? local.rules_url : local.local_rules_url}"

  // Shared Resources
  vaultName = "${var.raw_product}-${local.envInUse}"
  sharedResourceGroup = "${var.raw_product}-shared-${local.envInUse}"
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.env}"
  location = "${var.location}"

  tags = "${merge(var.common_tags,
      map("lastUpdated", "${timestamp()}")
      )}"
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

    SNL_RULES_URL = "${local.rules_url}"

    SNL_S2S_JWT_SECRET = "abc"
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

# region save DB details to Azure Key Vault
module "snl-vault" {
  source = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name = "${var.component}-${var.env}${substr(var.product, 3, -1)}"
  product = "${var.product}"
  env = "${var.env}"
  tenant_id = "${var.tenant_id}"
  object_id = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  product_group_object_id = "70de400b-4f47-4f25-a4f0-45e1ee4e4ae3"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name      = "${var.product}-${var.component}-POSTGRES-USER"
  value     = "${module.postgres-snl-events.user_name}"
  vault_uri = "${module.snl-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name      = "${var.product}-${var.component}-POSTGRES-PASS"
  value     = "${module.postgres-snl-events.postgresql_password}"
  vault_uri = "${module.snl-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name      = "${var.product}-${var.component}-POSTGRES-HOST"
  value     = "${module.postgres-snl-events.host_name}"
  vault_uri = "${module.snl-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name      = "${var.product}-${var.component}-POSTGRES-PORT"
  value     = "${module.postgres-snl-events.postgresql_listen_port}"
  vault_uri = "${module.snl-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name      = "${var.product}-${var.component}-POSTGRES-DATABASE"
  value     = "${module.postgres-snl-events.postgresql_database}"
  vault_uri = "${module.snl-vault.key_vault_uri}"
}
# endregion

# region shared Azure Key Vault
data "azurerm_key_vault" "snl-shared-vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.sharedResourceGroup}"
}

data "azurerm_key_vault_secret" "s2s_jwt_secret" {
  name      = "s2s-jwt-secret"
  vault_uri = "${data.azurerm_key_vault.snl-shared-vault.vault_uri}"
}

data "azurerm_key_vault_secret" "frontend_jwt_secret" {
  name      = "frontend-jwt-secret"
  vault_uri = "${data.azurerm_key_vault.snl-shared-vault.vault_uri}"
}
# endregion
