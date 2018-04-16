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
    SNL-EVENTS_DB_HOST = "${module.postgres-snl-events.host_name}"
    SNL-EVENTS_DB_PORT = "${module.postgres-snl-events.postgresql_listen_port}"
    SNL-EVENTS_DB_NAME = "${module.postgres-snl-events.postgresql_database}"
    SNL-EVENTS_DB_USERNAME = "${module.postgres-snl-events.user_name}"
    SNL-EVENTS_DB_PASSWORD = "${module.postgres-snl-events.postgresql_password}"
  }

}

module "postgres-snl-events" {
  source              = "git@github.com:contino/moj-module-postgres?ref=master"
  product             = "${var.product}-${var.component}"
  location            = "West Europe"
  env                 = "${var.env}"
  postgresql_user     = "snl-events"
}
