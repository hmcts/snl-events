locals {
  app_full_name = "${var.product}-${var.component}"
}
module "snl-api" {
  source               = "git@github.com:hmcts/moj-module-webapp"
  product              = "${var.product}-${var.component}"
  location             = "${var.location}"
  env                  = "${var.env}"
  ilbIp                = "${var.ilbIp}"
  is_frontend          = false
  subscription         = "${var.subscription}"
  additional_host_name = "${var.external_host_name}"

  app_settings = {
    # REDIS_HOST                   = "${module.redis-cache.host_name}"
    # REDIS_PORT                   = "${module.redis-cache.redis_port}"
    # REDIS_PASSWORD               = "${module.redis-cache.access_key}"
    # RECIPE_BACKEND_URL = "http://snl-recipe-backend-${var.env}.service.${data.terraform_remote_state.core_apps_compute.ase_name[0]}.internal"

  }
}
