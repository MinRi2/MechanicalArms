package mechanicalArms.logic;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class ArmsLStatementWriter{
    public static final String SPLITTER = " ";

    private StringBuilder builder;

    public void start(StringBuilder builder){
        this.builder = builder;
    }

    public void end(){
        // remove the last space
        builder.deleteCharAt(builder.length() - 1);
    }

    public ArmsLStatementWriter write(Object obj) {
        return write(String.valueOf(obj)).write(SPLITTER);
    }

    public ArmsLStatementWriter write(String str) {
        builder.append(str).append(SPLITTER);
        return this;
    }

    public ArmsLStatementWriter write(StringBuffer sb) {
        builder.append(sb).append(SPLITTER);
        return this;
    }

    public ArmsLStatementWriter write(CharSequence s) {
        builder.append(s).append(SPLITTER);
        return this;
    }

    public ArmsLStatementWriter write(CharSequence s, int start, int end) {
        builder.append(s, start, end).append(SPLITTER);
        return this;
    }

    public ArmsLStatementWriter write(char[] str) {
        builder.append(str).append(SPLITTER);
        return this;
    }

    public ArmsLStatementWriter write(char[] str, int offset, int len) {
        builder.append(str, offset, len).append(SPLITTER);
        return this;
    }

    public ArmsLStatementWriter write(boolean b) {
        builder.append(b).append(SPLITTER);
        return this;
    }

    public ArmsLStatementWriter write(char c) {
        builder.append(c).append(SPLITTER);
        return this;
    }

    public ArmsLStatementWriter write(int i) {
        builder.append(i).append(SPLITTER);
        return this;
    }

    public ArmsLStatementWriter write(long lng) {
        builder.append(lng).append(SPLITTER);
        return this;
    }

    public ArmsLStatementWriter write(float f) {
        builder.append(f).append(SPLITTER);
        return this;
    }

    public ArmsLStatementWriter write(double d) {
        builder.append(d).append(SPLITTER);
        return this;
    }
}
