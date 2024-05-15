package com.course.source.app.local.course

import com.course.source.app.course.CourseBean
import com.course.source.app.local.request.RequestContent
import com.course.source.app.local.request.RequestUnit
import com.course.source.app.local.source.webview.WebViewSourceService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 23:32
 */
object CourseApiInjector {

  fun init(requestContent: RequestContent<CourseBean>) {
    if (requestContent.requestUnits.isEmpty()) {
      requestContent.requestUnits.add(
        RequestUnit.create(
          contentKey = requestContent.key,
          serviceKey = "Web",
          id = 0,
        ).also {
          it.title.value = "课程-CQUPT"
          it.sourceData = Json.encodeToString(
            WebViewSourceService.WebViewData(
              url = "http://jwzx.cqupt.edu.cn/kebiao/kb_stu.php?xh={stuNum}",
              js = """
                const head = document.getElementById("head")
                const timeHead = head.children[0].children[1].textContent
                const nowWeek = parseInt(RegExp(' [0-9]{1,2} ').exec(timeHead)[0], 10);

                function getBeginLesson(node) {
                  switch (node) {
                    case "1、2节": return 1;
                    case "3、4节": return 3;
                    case "5、6节": return 5;
                    case "7、8节": return 7;
                    case "9、10节": return 9;
                    case "11、12节": return 11;
                  }
                }

                function getDayOfWeek(index) {
                  switch (index) {
                    case 0: return "MONDAY";
                    case 1: return "TUESDAY";
                    case 2: return "WEDNESDAY";
                    case 3: return "THURSDAY";
                    case 4: return "FRIDAY";
                    case 5: return "SATURDAY";
                    case 6: return "SUNDAY";
                  }
                }

                function getTime(index, beginLesson, length) {
                  const week = (() => {
                    switch (index) {
                      case 0: return "周一";
                      case 1: return "周二";
                      case 2: return "周三";
                      case 3: return "周四";
                      case 4: return "周五";
                      case 5: return "周六";
                      case 6: return "周七";
                    }
                  })()
                  const startTime = (() => {
                    switch (beginLesson) {
                      case 1: return "08:00";
                      case 3: return "10:15";
                      case 5: return "14:00";
                      case 7: return "16:20";
                      case 9: return "19:00";
                      case 11: return "20:50";
                    }
                  })()
                  const endTime = (() => {
                    switch (beginLesson + length - 1) {
                      case 1: return "08:45";
                      case 2: return "09:40";
                      case 3: return "11:00";
                      case 4: return "11:55";
                      case 5: return "14:45";
                      case 6: return "15:40";
                      case 7: return "17:00";
                      case 8: return "17:55";
                      case 9: return "19:45";
                      case 10: return "20:40";
                      case 11: return "21:35";
                      case 12: return "22:30";
                    }
                  })()
                  return `${str("week")} ${str("startTime")}-${str("endTime")}`
                }

                const tbody = document.getElementById("stuPanel").children[1].children[1];
                const lessons = Array.from(tbody.children).map((tr) => {
                  return Array.from(tr.children)
                    .slice(1)
                    .map((td, index) => {
                      return Array.from(td.children).map((kbTd) => {
                        const weekFlags = kbTd.getAttribute("zc").split('');
                        const texts = kbTd.innerHTML.split('<br>').map(text => {
                          return text.trim()
                            .replace(/\n/g, '')
                            .replace(/\t/g, '')
                            .replace(/<font color="#FF0000">/g, ' ')
                            .replace(/<\/font>/g, '')
                            .replace(/<span style="color:#0000FF">/g, '')
                            .replace(/<\/span>/g, '')
                            .replace(/"/g, '');
                        });
                        const rawWeek = texts[3];
                        const [teacher, type] = texts[4].split(' ').slice(0, 2);
                        const regex = RegExp('[1-9](?=节连上)');
                        const length = (regex.exec(rawWeek) || [])[0] ? parseInt(regex.exec(rawWeek)[0], 10) : 2;
                        const originClassroom = texts[2].substring(texts[2].indexOf('地点：') + 3);
                        let classroomSimplify = originClassroom;
                        if (originClassroom.includes('综合实验楼')) {
                          const result = originClassroom.match(RegExp("\\w+", 'g'))
                          if (result.length > 0) {
                            classroomSimplify = result.join('\n')
                          }
                        }
                        const beginLesson = getBeginLesson(tr.firstElementChild.textContent)
                        return {
                          course: texts[1].substring(texts[1].indexOf('-') + 1),
                          classroom: originClassroom,
                          classroomSimplify: classroomSimplify,
                          teacher: teacher,
                          courseNum: texts[0],
                          weeks: weekFlags.map((flag, index) => {
                            if (flag === '1') {
                              return index + 1;
                            }
                            return null;
                          }).filter(week => week !== null),
                          dayOfWeek: getDayOfWeek(index),
                          beginLesson: beginLesson,
                          length: length,
                          showOptions: [
                            {first: "周期", second: texts[3]},
                            {first: "时间", second: getTime(index, beginLesson, length)},
                            {first: "课程类型", second: type},
                          ],
                        }
                      }).flat();
                    })
                    .flat();
                }).flat();

                const term = head.children[0].children[0].children[0].children[1].textContent
                const year = RegExp('\\d{4}').exec(term)[0]
                const topOrBottom = parseInt(RegExp('年\\d').exec(term)[0].charAt(1))
                const startYear = parseInt("{stuNum}".substring(0, 4))
                const termIndex = (year - startYear) * 2 + topOrBottom - 1

                dataBridge.success(JSON.stringify({
                  beginDate: (() => {
                    const date = new Date();
                    const dayOfWeek = date.getDay();
                    const offsetDay = (nowWeek - 1) * 7 + (dayOfWeek === 0 ? 6 : dayOfWeek - 1);
                    const beginDate = new Date(date.getTime() - offsetDay * 24 * 60 * 60 * 1000);
                    const year = beginDate.getFullYear();
                    const month = beginDate.getMonth() + 1;
                    const day = beginDate.getDate();
                    return `${str("year")}-${str("month")}-${str("day")}`;
                  })(),
                  term: (() => {
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
                        dataSource.error(`未知学期，termIndex = ${str("termIndex")}`)
                        throw new Error(`未知学期，termIndex = ${str("termIndex")}`);
                    }
                  })(),
                  termIndex: termIndex,
                  lessons: lessons,
                }));
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