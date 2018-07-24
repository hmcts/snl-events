variable "product" {
  type        = "string"
  default     = "snl"
  description = "Scheduling and Listing "
}
variable "component" {
  type        = "string"
  default     = "events"
  description = "Scheduling and Listing "
}
variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type        = "string"
  description = "(Required) The environment in which to deploy the application infrastructure."
}

variable "ilbIp" {}

variable "tenant_id" {
  type                  = "string"
  description           = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environemnt variables and not normally required to be specified."
}

variable "object_id" {
  type                  = "string"
  description           = "(Required) The object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies. This is usually sourced from environemnt variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  type                        = "string"
  description                 = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "external_host_name" {
  default = "snl-events.reform.hmcts.net"
}
variable "subscription" {}

variable "appinsights_instrumentation_key" {
  description = "Instrumentation key of the App Insights instance this webapp should use. Module will create own App Insights resource if this is not provided."
  default = ""
}

variable "db_user" {
  type        = "string"
  default     = "snlevents"
  description = "Database user name"
}

variable "db_name" {
  type        = "string"
  default     = "snleventsdb"
  description = "Database name"
}

variable "common_tags" {
  type = "map"
}
