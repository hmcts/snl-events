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

  app_settings = {
    DEFINITION_STORE_DB_HOST = "${module.postgres-sln-events.host_name}"
    DEFINITION_STORE_DB_PORT = "${module.postgres-sln-events.postgresql_listen_port}"
    DEFINITION_STORE_DB_NAME = "${module.postgres-sln-events.postgresql_database}"
    DEFINITION_STORE_DB_USERNAME = "${module.postgres-sln-events.user_name}"
    DEFINITION_STORE_DB_PASSWORD = "${module.postgres-sln-events.postgresql_password}"
  }

}

module "postgres-sln-events" {
  source              = "git@github.com:contino/moj-module-postgres?ref=master"
  product             = "${var.product}-${var.component}"
  location            = "West Europe"
  env                 = "${var.env}"
  postgresql_user     = "snl-events"
}
