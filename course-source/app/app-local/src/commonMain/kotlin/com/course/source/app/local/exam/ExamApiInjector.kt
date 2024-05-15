package com.course.source.app.local.exam

import com.course.source.app.exam.ExamTermBean
import com.course.source.app.local.request.RequestContent
import com.course.source.app.local.request.RequestUnit
import com.course.source.app.local.source.webview.WebViewSourceService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 23:48
 */
object ExamApiInjector {

  fun init(requestContent: RequestContent<List<ExamTermBean>>) {
    if (requestContent.requestUnits.isEmpty()) {
      requestContent.requestUnits.add(
        RequestUnit.create(
          contentKey = requestContent.key,
          serviceKey = "Web",
          id = 0,
        ).also {
          it.title.value = "考试-CQUPT"
          it.sourceData = Json.encodeToString(
            WebViewSourceService.WebViewData(
              url = "http://jwzx.cqupt.edu.cn/ksap/showKsap.php?type=stu&id={stuNum}",
              js = """
                const tbodyElement = document.querySelector('.printTable table tbody');
                const trElements = tbodyElement.querySelectorAll('tr');
                const data = [];

                trElements.forEach(function(trElement) {
                  const array = []
                  const tdElements = trElement.querySelectorAll('td')
                  tdElements.forEach(function(tdElement) {
                    array.push(tdElement.textContent)
                  })
                  const week = parseInt(array[6].substring(0, array[6].indexOf("周")))
                  const startTimeStr = array[8].substring(array[8].indexOf("节") + 2, array[8].lastIndexOf("-"))
                  const startTimeHour = parseInt(startTimeStr.split(":")[0])
                  const startTimeMinute = parseInt(startTimeStr.split(":")[1])
                  const endTimeStr = array[8].substring(array[8].lastIndexOf("-") + 1)
                  const endTimeHour = parseInt(endTimeStr.split(":")[0])
                  const endTimeMinute = parseInt(endTimeStr.split(":")[1])
                  const minuteDuration = (endTimeHour - startTimeHour) * 60 + endTimeMinute - startTimeMinute
                  data.push({
                    week: week,
                    startTimeHour: startTimeHour,
                    startTimeMinute: startTimeMinute,
                    dayOfWeekNumber: array[7],
                    minuteDuration: minuteDuration,
                    course: array[5],
                    courseNum: array[4],
                    classroom: array[9],
                    type: array[3],
                    seat: array[10],
                  })
                });

                const termStr = head.children[0].children[0].children[0].children[1].textContent
                const year = RegExp('\\d{4}').exec(termStr)[0]
                const topOrBottom = parseInt(RegExp('年\\d').exec(termStr)[0].charAt(1))
                const startYear = parseInt("{stuNum}".substring(0, 4))
                const termIndex = (year - startYear) * 2 + topOrBottom - 1
                const term = (() => {
                  switch (termIndex) {
                    case 0: return "大一上"
                    case 1: return "大一下"
                    case 2: return "大二上"
                    case 3: return "大二下"
                    case 4: return "大三上"
                    case 5: return "大三下"
                    case 6: return "大四上"
                    case 7: return "大四下"
                    case 8: return "大五上"
                    case 9: return "大五下"
                    case 10: return "大六上"
                    case 11: return "大六下"
                    case 12: return "大七上"
                    case 13: return "大七下"
                    default:
                      dataBridge.error(`未知学期，termIndex = ${str("termIndex")}`)
                      throw new Error(`未知学期，termIndex = ${str("termIndex")}`);
                  }
                })()

                dataBridge.load('http://jwzx.cqupt.edu.cn/ksap/index.php')
                dataBridge.onLoad = function (htmlText) {
                  const nowWeek = htmlText.match(/第 (\d+) 周/)[1]
                  const date = new Date();
                  const dayOfWeek = date.getDay();
                  const offsetDay = (nowWeek - 1) * 7 + (dayOfWeek === 0 ? 6 : dayOfWeek - 1);
                  const beginDate = new Date(date.getTime() - offsetDay * 24 * 60 * 60 * 1000);
                  const year = beginDate.getFullYear();
                  const month = beginDate.getMonth() + 1;
                  const day = beginDate.getDate();
                  dataBridge.println("success")
                  dataBridge.success(JSON.stringify([{
                    term: term,
                    termIndex: termIndex,
                    beginDate: `${str("year")}-${str("month")}-${str("day")}`,
                    exams: data.map((it) => {
                      const startDate = new Date(beginDate.getTime() +
                        (it.week - 1) * 7 * 24 * 60 * 60 * 1000 +
                        (it.dayOfWeekNumber - 1) * 24 * 60 * 60 * 1000
                      )
                      it.startTime = `${str("startDate.getFullYear()")}-${str("startDate.getMonth() + 1")}-${str("startDate.getDate()")} ${str("it.startTimeHour")}:${str("it.startTimeMinute")}`
                      return it
                    })
                  }]))
                }

              """.trimIndent(),
            )
          )
        }
      )
    }
  }

  private fun str(name: String): String {
    return "\${$name}"
  }
}