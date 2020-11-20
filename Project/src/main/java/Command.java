public class Command {
    private char argument;
    private String projectTarget;
    private String changeInfo;
    public char getArgument() {
        return argument;
    }

    public void setArgument(char argument) {
        this.argument = argument;
    }

    public String getProjectTarget() {
        return projectTarget;
    }

    public void setProjectTarget(String projectTarget) {
        this.projectTarget = projectTarget;
    }

    public String getChangeInfo() {
        return changeInfo;
    }

    public void setChangeInfo(String changeInfo) {
        this.changeInfo = changeInfo;
    }
    Command(char arg, String projectTarget, String changeInfo){
        this.argument = arg;
        this.projectTarget = projectTarget;
        this.changeInfo =  changeInfo;
    }


}
