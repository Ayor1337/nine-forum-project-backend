#!/usr/bin/env bash

set -euo pipefail

check_only=false
case "${1:-}" in
  '') ;;
  --check-only) check_only=true ;;
  *) echo "Usage: $0 [--check-only]" >&2; exit 2 ;;
esac

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
environment_dir="$script_dir/environment"
compose_file="$script_dir/docker-compose.yaml"
export FORUM_HOME='/docker_volumes/nine_forum'
elasticsearch_data_dir="$FORUM_HOME/elastic/data"
elasticsearch_plugins_dir="$FORUM_HOME/elastic/plugins"

prepare_elasticsearch_directories() {
  local data_owner
  local plugins_owner
  data_owner="$(stat -c '%u:%g' "$elasticsearch_data_dir" 2>/dev/null || true)"
  plugins_owner="$(stat -c '%u:%g' "$elasticsearch_plugins_dir" 2>/dev/null || true)"
  if [[ "$data_owner" == '1000:0' && "$plugins_owner" == '1000:0' ]]; then
    return
  fi

  local -a privilege_command=()
  if [[ "$(id -u)" -ne 0 ]]; then
    command -v sudo >/dev/null 2>&1 || {
      echo 'Elasticsearch 数据目录需要 root 权限初始化，但当前系统未安装 sudo。' >&2
      exit 1
    }
    privilege_command=(sudo)
  fi

  "${privilege_command[@]}" install -d -o 1000 -g 0 -m 0770 \
    "$elasticsearch_data_dir" "$elasticsearch_plugins_dir"
  "${privilege_command[@]}" chown -R 1000:0 \
    "$elasticsearch_data_dir" "$elasticsearch_plugins_dir"
}

read_env_value() {
  local path="$1"
  local name="$2"
  awk -v key="$name" 'index($0, key "=") == 1 { print substr($0, length(key) + 2); exit }' "$path"
}

is_usable_env_value() {
  local value="${1:-}"
  value="${value#"${value%%[![:space:]]*}"}"
  value="${value%"${value##*[![:space:]]}"}"
  [[ -n "$value" && "$value" != CHANGE_ME* ]]
}

problems=()

check_env_file() {
  local name="$1"
  shift
  local path="$environment_dir/$name"
  if [[ ! -f "$path" ]]; then
    problems+=("缺少环境变量文件：$path（请从 $name.example 复制）")
    return
  fi

  local field
  local value
  for field in "$@"; do
    value="$(read_env_value "$path" "$field")"
    if ! is_usable_env_value "$value"; then
      problems+=("$name 缺少或未配置字段：$field")
    fi
  done
}

check_env_file mysql.env \
  MYSQL_ROOT_PASSWORD MYSQL_DATABASE MYSQL_USER MYSQL_PASSWORD
check_env_file redis.env \
  REDIS_USERNAME REDIS_PASSWORD
check_env_file minio.env \
  MINIO_ROOT_USER MINIO_ROOT_PASSWORD
check_env_file rabbitmq.env \
  RABBITMQ_DEFAULT_USER RABBITMQ_DEFAULT_PASS RABBITMQ_DEFAULT_VHOST
check_env_file elasticsearch.env \
  ELASTIC_PASSWORD ELASTICSEARCH_APP_USERNAME ELASTICSEARCH_APP_PASSWORD ELASTICSEARCH_APP_ROLE

if ((${#problems[@]} > 0)); then
  echo '环境变量检查失败：' >&2
  printf -- '- %s\n' "${problems[@]}" >&2
  exit 1
fi

echo '环境变量检查通过；未修改任何 env 文件。'

if [[ "$check_only" == true ]]; then
  exit 0
fi

prepare_elasticsearch_directories
docker compose -f "$compose_file" up -d mysql redis minio rabbitmq elasticsearch elasticsearch-init
echo 'MySQL、Redis、MinIO、RabbitMQ、Elasticsearch 已启动。'
