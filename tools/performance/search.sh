#!/bin/bash
startDate=`date -d "2018-06-01 10:00"`
weeks=12
judgeId=584a3ac1-690e-41b5-913f-b51cca824a45
roomId=74cc0d1e-0e1e-4f6e-9ae6-7fd9d223fb81

echo "1 DAY"
curl -o /dev/null -s -w '%{time_total} :%{http_code} %{url_effective}\n' "http://localhost:8091/search?from=2018-06-25%2010:00&to=2018-06-25%2012:00&duration=600"
curl -o /dev/null -s -w '%{time_total} :%{http_code} %{url_effective}\n' "http://localhost:8091/search?from=2018-06-25%2010:00&to=2018-06-25%2012:00&duration=600&judgeId=$judgeId"
curl -o /dev/null -s -w '%{time_total} :%{http_code} %{url_effective}\n' "http://localhost:8091/search?from=2018-06-25%2010:00&to=2018-06-25%2012:00&duration=600&judgeId=$judgeId&roomId=$roomId"


echo "start $(date -d "$startDate" +%Y-%m-%d)"
echo "weeks $weeks"


for ((i_week=1; i_week<=$weeks; i_week++)); do
    echo "$i_week WEEK"
    endDate=`date -d "$startDate +$i_week weeks"`
curl -o /dev/null -s -w '%{time_total} :%{http_code} %{url_effective}\n' "http://localhost:8091/search?from=$(date -d "$startDate" +%Y-%m-%d)%2010:00&to=$(date -d "$endDate" +%Y-%m-%d)%2012:00&duration=600"
curl -o /dev/null -s -w '%{time_total} :%{http_code} %{url_effective}\n' "http://localhost:8091/search?from=$(date -d "$startDate" +%Y-%m-%d)%2010:00&to=$(date -d "$endDate" +%Y-%m-%d)%2012:00&duration=600&judgeId=$judgeId"
curl -o /dev/null -s -w '%{time_total} :%{http_code} %{url_effective}\n' "http://localhost:8091/search?from=$(date -d "$startDate" +%Y-%m-%d)%2010:00&to=$(date -d "$endDate" +%Y-%m-%d)%2012:00&duration=600&judgeId=$judgeId&roomId=$roomId"

	echo $(date -d "$endDate" +%Y-%m-%d)
done



