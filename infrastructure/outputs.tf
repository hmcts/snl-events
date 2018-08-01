output "frontend_deployment_endpoint" {
  value = "${module.snl-events.gitendpoint}"
}
output "microserviceName" {
  value = "${local.app_full_name}"
}

output "vaultName" {
  value = "${module.snl-vault.key_vault_name}"
}
