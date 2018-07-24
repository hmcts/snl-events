output "frontend_deployment_endpoint" {
  value = "${module.snl-events.gitendpoint}"
}
output "microserviceName" {
  value = "${local.app_full_name}"
}

output "vaultName" {
  value = "${local.vaultName}"
}
