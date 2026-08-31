#!/usr/bin/env bash

set -euo pipefail

prepare_only=false
case "${1:-}" in
  '') ;;
  --prepare-only) prepare_only=true ;;
  *) echo "Usage: $0 [--prepare-only]" >&2; exit 2 ;;
esac

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
project_dir="$(dirname -- "$script_dir")"
environment_dir="$script_dir/environment"
application_config="$project_dir/web/web-app/src/main/resources/application.yml"
compose_file="$script_dir/docker-compose.yaml"

read_env_value() {
  local path="$1"
  local name="$2"
  [[ -f "$path" ]] || return 0
  awk -v key="$name" 'index($0, key "=") == 1 { print substr($0, length(key) + 2); exit }' "$path"
}

usable_value() {
  local value="${1:-}"
  [[ -n "$value" && "$value" != CHANGE_ME* ]] || return 1
  printf '%s' "$value"
}

yaml_scalar() {
  local path="$1"
  local wanted="$2"
  awk -v wanted="$wanted" '
    function trim(value) {
      sub(/^[[:space:]]+/, "", value)
      sub(/[[:space:]]+$/, "", value)
      return value
    }
    /^[[:space:]]*[A-Za-z][A-Za-z0-9.-]*:/ {
      match($0, /^[[:space:]]*/)
      indent = RLENGTH
      if (indent % 2 != 0) next
      level = indent / 2

      line = substr($0, indent + 1)
      separator = index(line, ":")
      key = substr(line, 1, separator - 1)
      value = trim(substr(line, separator + 1))

      for (index_level in parents) {
        if (index_level >= level) delete parents[index_level]
      }
      if (value == "") {
        parents[level] = key
        next
      }

      actual = ""
      for (index_level = 0; index_level < level; index_level++) {
        if (!(index_level in parents)) {
          actual = ""
          break
        }
        actual = actual (actual == "" ? "" : ".") parents[index_level]
      }
      actual = actual (actual == "" ? "" : ".") key
      if (actual != wanted) next

      if ((substr(value, 1, 1) == "\"" && substr(value, length(value), 1) == "\"") ||
          (substr(value, 1, 1) == "\047" && substr(value, length(value), 1) == "\047")) {
        value = substr(value, 2, length(value) - 2)
      }
      print value
      exit
    }
  ' "$path"
}

require_value() {
  local value="${1:-}"
  local description="$2"
  if [[ -z "$value" ]]; then
    echo "无法从旧配置读取 $description；请先检查 $application_config" >&2
    exit 1
  fi
  printf '%s' "$value"
}

new_local_secret() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -base64 32 | tr -d '\r\n'
  else
    od -An -N32 -tx1 /dev/urandom | tr -d ' \r\n'
  fi
}

write_env_file() {
  local name="$1"
  shift
  local path="$environment_dir/$name"
  : >"$path"
  while (($# > 0)); do
    printf '%s=%s\n' "$1" "$2" >>"$path"
    shift 2
  done
  chmod 600 "$path"
}

if [[ ! -f "$application_config" ]]; then
  echo "缺少旧的本地配置：$application_config" >&2
  exit 1
fi

mkdir -p "$environment_dir"

mysql_env="$environment_dir/mysql.env"
redis_env="$environment_dir/redis.env"
elasticsearch_env="$environment_dir/elasticsearch.env"

old_database_password="$(require_value "$(yaml_scalar "$application_config" spring.datasource.password)" spring.datasource.password)"
mysql_root_password="$(read_env_value "$mysql_env" MYSQL_ROOT_PASSWORD)"
mysql_root_password="$(usable_value "$mysql_root_password" || printf '%s' "$old_database_password")"
mysql_database="$(read_env_value "$mysql_env" MYSQL_DATABASE)"
mysql_database="$(usable_value "$mysql_database" || printf '%s' nine_forum)"

write_env_file mysql.env \
  MYSQL_ROOT_PASSWORD "$mysql_root_password" \
  MYSQL_DATABASE "$mysql_database" \
  MYSQL_USER nine_forum_app \
  MYSQL_PASSWORD "$old_database_password"

redis_username="$(read_env_value "$redis_env" REDIS_USERNAME)"
redis_username="$(usable_value "$redis_username" || printf '%s' nineforum-app)"
redis_password="$(read_env_value "$redis_env" REDIS_PASSWORD)"
redis_password="$(usable_value "$redis_password" || new_local_secret)"
write_env_file redis.env \
  REDIS_USERNAME "$redis_username" \
  REDIS_PASSWORD "$redis_password"

write_env_file minio.env \
  MINIO_ROOT_USER "$(require_value "$(yaml_scalar "$application_config" spring.minio.access-key)" spring.minio.access-key)" \
  MINIO_ROOT_PASSWORD "$(require_value "$(yaml_scalar "$application_config" spring.minio.secret-key)" spring.minio.secret-key)"

write_env_file rabbitmq.env \
  RABBITMQ_DEFAULT_USER "$(require_value "$(yaml_scalar "$application_config" spring.rabbitmq.username)" spring.rabbitmq.username)" \
  RABBITMQ_DEFAULT_PASS "$(require_value "$(yaml_scalar "$application_config" spring.rabbitmq.password)" spring.rabbitmq.password)" \
  RABBITMQ_DEFAULT_VHOST "$(require_value "$(yaml_scalar "$application_config" spring.rabbitmq.virtual-host)" spring.rabbitmq.virtual-host)"

elastic_password="$(read_env_value "$elasticsearch_env" ELASTIC_PASSWORD)"
elastic_password="$(usable_value "$elastic_password" || new_local_secret)"
elasticsearch_app_password="$(read_env_value "$elasticsearch_env" ELASTICSEARCH_APP_PASSWORD)"
elasticsearch_app_password="$(usable_value "$elasticsearch_app_password" || new_local_secret)"
write_env_file elasticsearch.env \
  ELASTIC_PASSWORD "$elastic_password" \
  ELASTICSEARCH_APP_USERNAME nineforum_app \
  ELASTICSEARCH_APP_PASSWORD "$elasticsearch_app_password" \
  ELASTICSEARCH_APP_ROLE nineforum_app

echo '本地凭据文件已准备完成；旧 MySQL、MinIO、RabbitMQ 凭据已复用，新增认证密码已随机生成。'
echo '凭据值只保存在被 Git 忽略的 .docker/environment/*.env 中。'

if [[ "$prepare_only" == true ]]; then
  exit 0
fi

# Elasticsearch 未包含在默认启动列表中：现有本地数据卷来自 9.2.1，不能直接交给 8.18.8。
docker compose -f "$compose_file" up -d mysql redis minio rabbitmq
echo 'MySQL、Redis、MinIO、RabbitMQ 已启动。Elasticsearch 请在新卷或完成备份迁移后单独启动。'
