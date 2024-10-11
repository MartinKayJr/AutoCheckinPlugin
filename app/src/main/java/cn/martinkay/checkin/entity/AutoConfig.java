package cn.martinkay.checkin.entity;

public class AutoConfig {
    // 规则名
    private String name;
    // 触发时间
    private String activeTime;
    // 触发星期
    private String activeWeek;
    // 触发次数
    private Integer activeCount;
    // 触发次数大于1时，下次触发间隔时间。
    private Integer nextActiveCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(String activeTime) {
        this.activeTime = activeTime;
    }

    public String getActiveWeek() {
        return activeWeek;
    }

    public void setActiveWeek(String activeWeek) {
        this.activeWeek = activeWeek;
    }

    public Integer getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(Integer activeCount) {
        this.activeCount = activeCount;
    }

    public Integer getNextActiveCount() {
        return nextActiveCount;
    }

    public void setNextActiveCount(Integer nextActiveCount) {
        this.nextActiveCount = nextActiveCount;
    }
}
