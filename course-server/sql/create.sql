create table course
(
    course_num  varchar(20) not null
        primary key,
    course_name varchar(50) not null,
    course_type varchar(20) not null
);

create table course_class
(
    class_num  varchar(20) not null
        primary key,
    course_num varchar(20) not null
);

create table course_class_stu
(
    class_num varchar(20) not null,
    stu_num   varchar(10) not null,
    primary key (class_num, stu_num)
);

create table exam
(
    course_num      varchar(20) not null,
    exam_type       varchar(10) not null,
    start_time      char(16)    not null,
    minute_duration int         not null,
    primary key (course_num, exam_type)
);

create table user
(
    user_id  int auto_increment
        primary key,
    password char(64)    not null,
    type     varchar(10) not null,
    token    text        null
);

create table notification
(
    notification_id int auto_increment
        primary key,
    user_id         int  not null,
    time            int  not null,
    content         text not null,
    constraint notification_user_userId_fk
        foreign key (user_id) references user (user_id)
);

create table notification_status
(
    user_id int        not null
        primary key,
    has_new tinyint(1) not null,
    constraint notification_status_user_userId_fk
        foreign key (user_id) references user (user_id)
);

create table schedule
(
    title            varchar(20) not null,
    description      text        not null,
    start_time       char(16)    not null,
    minute_duration  int         not null,
    repeat_content   text        not null,
    text_color       char(8)     not null,
    background_color char(8)     not null,
    user_id          int         not null,
    schedule_id      int auto_increment
        primary key,
    constraint schedule_user_userId_fk
        foreign key (user_id) references user (user_id)
);

create table student
(
    name       varchar(10) not null,
    major      varchar(50) not null,
    entry_year int         not null,
    stu_num    varchar(10) not null
        primary key,
    user_id    int         not null,
    constraint student_user_userId_fk
        foreign key (user_id) references user (user_id)
);

create table exam_stu
(
    course_num varchar(20) not null,
    exam_type  varchar(10) not null,
    stu_num    varchar(10) not null,
    classroom  varchar(20) not null,
    seat       varchar(10) not null,
    primary key (course_num, exam_type, stu_num),
    constraint exam_stu_exam_course_num_exam_type_fk
        foreign key (course_num, exam_type) references exam (course_num, exam_type),
    constraint exam_stu_student_stu_num_fk
        foreign key (stu_num) references student (stu_num)
);

create table teacher
(
    tea_num varchar(10) not null
        primary key,
    name    varchar(20) not null,
    major   varchar(20) not null,
    user_id int         not null,
    constraint teacher_user_userId_fk
        foreign key (user_id) references user (user_id)
);

create table course_class_plan
(
    class_plan_id int auto_increment
        primary key,
    class_num     varchar(20) not null,
    tea_num       varchar(10) not null,
    date          char(10)    not null,
    begin_lesson  int         not null,
    length        int         not null,
    classroom     varchar(20) not null,
    is_newly      tinyint(1)  not null,
    constraint course_class_plan_course_class_class_num_fk
        foreign key (class_num) references course_class (class_num),
    constraint course_class_plan_teacher_tea_num_fk
        foreign key (tea_num) references teacher (tea_num)
);

create table attendance_code
(
    class_plan_id     int         not null,
    code              varchar(10) not null,
    publish_timestamp mediumtext  not null,
    late_timestamp    mediumtext  not null,
    finish_timestamp  mediumtext  not null,
    primary key (class_plan_id, code),
    constraint attendance_code_course_class_plan_class_plan_id_fk
        foreign key (class_plan_id) references course_class_plan (class_plan_id)
);

create table attendance_leave
(
    leave_id      int auto_increment
        primary key,
    class_plan_id int         not null,
    stu_num       varchar(10) not null,
    timestamp     mediumtext  not null,
    reason        text        not null,
    status        varchar(20) not null,
    constraint attendance_leave_course_class_plan_class_plan_id_fk
        foreign key (class_plan_id) references course_class_plan (class_plan_id),
    constraint attendance_leave_student_stu_num_fk
        foreign key (stu_num) references student (stu_num)
);

create table attendance_stu
(
    class_plan_id int         not null,
    code          varchar(10) not null,
    stu_num       varchar(10) not null,
    timestamp     mediumtext  not null,
    status        varchar(20) not null,
    is_modified   tinyint(1)  not null,
    primary key (class_plan_id, code, stu_num),
    constraint attendance_stu_attendance_code_class_plan_id_code_fk
        foreign key (class_plan_id, code) references attendance_code (class_plan_id, code),
    constraint attendance_stu_student_stu_num_fk
        foreign key (stu_num) references student (stu_num)
);

create table team
(
    team_id     int auto_increment
        primary key,
    team_name   varchar(20) not null,
    description text        not null,
    admin_id    int         not null,
    constraint team_user_userId_fk
        foreign key (admin_id) references user (user_id)
);

create table team_member
(
    team_id                int         not null,
    user_id                int         not null,
    role                   varchar(20) not null,
    identity               varchar(50) null,
    is_confirmed           tinyint(1)  not null,
    invite_notification_id int         null,
    primary key (team_id, user_id),
    constraint team_member_notification_notification_id_fk
        foreign key (invite_notification_id) references notification (notification_id),
    constraint team_member_team_teamId_fk
        foreign key (team_id) references team (team_id),
    constraint team_member_user_userId_fk
        foreign key (user_id) references user (user_id)
);

create table team_schedule
(
    team_id          int         not null,
    title            varchar(20) not null,
    content          text        not null,
    start_time       char(16)    not null,
    minute_duration  int         not null,
    repeat_content   text        not null,
    text_color       char(8)     not null,
    background_color char(8)     not null,
    sender_id        int         not null,
    team_schedule_id int auto_increment
        primary key,
    constraint team_schedule_team_teamId_fk
        foreign key (team_id) references team (team_id),
    constraint team_schedule_user_userId_fk
        foreign key (sender_id) references user (user_id)
);