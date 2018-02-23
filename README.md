# correction
## correction简介
```
针对给定的字符串，在限定的词库范围内进行纠错，使用场景如：电影名纠错，音乐名纠错等。纠错的范围有同音字纠错（狼牙棒->琅琊榜），同义词纠错（单亲母亲->单亲妈妈），语序颠倒纠错(曼侧斯特的海边->海边的曼侧斯特)。项目依赖于solr和consul，其中solr用于初步的检索，consul用于词库的动态加载。
```

## correction依赖服务的安装
### clone工程
```
git clone git@github.com:yanbinwa/correction-solr.git
cd correction-solr
git checkout -b correction_standard origin/correction_standard
ROOT_PATH=$(pwd)
```

### 安装Solr的docker镜像（如果环境中已安装Solr，可以跳过该步骤）
```
1. 配置Solr的data目录映射(假设Solr中data目录在主机上的目录为"SOLR_DATA_PATH")

sed 's#XXX#SOLR_DATA_PATH#g' solr/idc.env.template > solr/idc.env
rm -rf SOLR_DATA_PATH
mkdir -p SOLR_DATA_PATH

2. 启动Solr的docker镜像

./solr/run.sh ./solr/idc.env

3. 验证Solr是否启动成功

在浏览器中输入http://localhost:8081

```

### 配置Solr
```
cd $ROOT_PATH

1. 在Solr中新增correction_standard core

tar zxvf ./solr/data/correction_standard.tar.gz -C SOLR_DATA_PATH
sudo chown -R 8983:8983 SOLR_DATA_PATH

2. 重启Solr，使配置生效

docker restart solr

3. 验收Solr的配置是否生效

在浏览器中输入http://localhost:8081，查看是否有correction_standard core

```

### 安装Consul的docker镜像（如果环境中已安装Consul，可以跳过该步骤）
```
cd $ROOT_PATH

1. 配置Consul的data映射目录(假设Consul中data目录在主机上的目录为"CONSUL_DATA_PATH")

sed 's#XXX#CONSUL_DATA_PATH#g' consul/idc.env.template > consul/idc.env
rm -rf CONSUL_DATA_PATH
mkdir -p CONSUL_DATA_PATH

2. 启动Consul的docker镜像

./consul/run.sh ./consul/idc.env

3. 验证Consul是否启动成功

在浏览器中输入http://localhost:8500

```

### 配置Consul
```
1. 在consul中写入词库信息

curl -XPUT 'http://localhost:8500/v1/kv/idc/templates/5a200ce8e6ec3a6506030e54ac3b970e' -d '{"version":"1","url":"http://172.17.0.1/Files/settings/correction/dictionary.txt"}'

说明：
以上命令是在consul的/idc/templates/5a200ce8e6ec3a6506030e54ac3b970e目录下写入词库信息

{
    "version":"1",
    "url":"http://172.17.0.1/Files/settings/correction/dictionary.txt"
}

目录中的5a200ce8e6ec3a6506030e54ac3b970e是appid，可以支持多个appid，每个appid对应不同的词库信息
version: 指的是词库的版本号，当版本后修改后，服务会自动加载新的词库文件
url: 指的是词库的下载路径

2. 将词库文件上传，确保文件可以通过url下载

3. 词库文件的格式

field + “\t” + 词条
例如
“video“ + ”\t" + "大话西游"

项目提供了电影名称和音乐名称的词库文件范例，具体路径在file/dictionary.txt

```

## correction安装

### correction配置
```
correction的配置位于docker/idc.env文件中
```

#### 说明
| 参数 | 类型	| 可选 | 说明 | 举例
------|------|------|------|------
CS_HOST_SERVER_PORT	| int	| 必须	| 纠错服务在宿主机的端口号	|	9101
CS_SERVER_PORT	| int	| 必须	| 纠错服务在docker中的端口号	|	9101
SOLR_URL_KEY	| String	| 必须	| solr服务的url	|	http://172.16.101.61:8081/solr/correction_standard
ORIGIN_FILE_PATH_KEY	| String	| 必须	| 词库文件路径，已经弃用	|	
COMMON_SENTENCE_FILE_PATH_KEY	| String	| 必须	| 模板文件路径，已经弃用	|
COMMAND_FILE_PATH_KEY	| String	| 必须	| 指令词库文件路径，已经弃用	|
TOMCAT_MAX_CONNECTION_KEY	| int	| 必须	| Tomcat的最大连接数	|	100
TOMCAT_MAX_THREAD_KEY	| int	| 必须	| Tomcat请求处理的最大线程数	|	100
TOMCAT_MAX_TIMEOUT_KEY	| int	| 必须	| Tomcat连接的超时	|	30000
CONSUL_SERVICE_URL_KEY	| String	| 必须	| Consul服务的url	|	http://172.16.101.61:8500/
CONSUL_KEY_PREFIX_KEY	| String	| 必须	| 词库信息在Consul中的路径，不包含appid	|	idc/correction
RUN_ON_LOCAL_KEY	| boolean	| 必须	| 代码调试配置，如果为true，会将词库下载Url的IP替换（例如172.17.0.1 –>172.16.101.61），保证代码调试正常运行，生成环境应设为false	|	false
ENABLE_HOMONYM_CORRECTION_KEY	| boolean	| 必须	| 是否支持同音词纠错	|	true
ENABLE_SYNONYM_CORRECTION_KEY	| boolean	| 必须	| 是否支持同义词纠错	|	true
ENABLE_INVERT_ORDER_CORRECTION_KEY	| boolean	| 必须	| 是否支持语序颠倒纠错	|	true
MAX_RECOMMEND_NUM_KEY	| int	| 必须	| 最多输出结果的个数，即当返回纠错结果中有得分相近的，可以作为推荐输出	|	1
CORRECTION_THRESHOLD_LEVEL_KEY	| int	| 必须	| 值为0、1、2，对应输出结果得分的阈值；0为最低阈值，即尽量给出纠错结果，但同时提升给出错误的纠错结果的几率；2为最高阈值，即宁可不给出纠错结果，也不x希望给出错误的纠错结果；1为这两者之间	|	1
CS_LOG_LEVEL	| String	| 必须	| 服务的日志配置	|	INFO,stdout,file

### 打包、安装Correction Docker
```
cd $ROOT_PATH
TAG=$(git rev-parse --short HEAD)
mvn deploy -Dmaven.test.skip=true
./docker/build.sh 
docker push docker-reg.emotibot.com.cn:55688/correction-standard:$TAG
./docker/run.sh ./docker/idc.env $TAG
```

## correction调用
```
GET /correction/getCorrectionName
```
### 请求
| 参数 | 类型	| 可选 | 说明	| 举例
------|------|------|------|------
appid	| string	| 必须	| APPID	|	5a200ce8e6ec3a6506030e54ac3b970e
fields	| string	| 必须	| 可以输入多个field，中间通过","隔开	|	music,video
text		| string	| 必须	| 需要纠错的字符串	|	我们的哎

### 响应
```
{
  "old_text": "我们的哎",
  "likely_names": [
    {
      "name": "我们的爱",
      "field": "music"
    }
  ]
}
```
