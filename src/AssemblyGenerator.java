import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class AssemblyGenerator {
    private int indent_level = 0;

    private static final String ERROR_IO_CLOSE =
            "Unable to close fileWriter";
    private static final String ERROR_IO_CONSTRUCT =
            "Unable to construct FileWriter for file %s";
    private static final String ERROR_IO_WRITE =
            "Unable to write to fileWriter";

    private FileWriter fileWriter;

    private static final String FILE_HEADER = "/*\n" +
                                              " * Generated %s\n" +
                                              " */\n\n";

    private static final String SEPARATOR = "\t";

    private static final StringBuilder strBuilder = new StringBuilder();

    public AssemblyGenerator(String fileToWrite) {
        try {
            fileWriter = new FileWriter(fileToWrite);

            writeAssembly(FILE_HEADER, (new Date()).toString());
        } catch (IOException e) {
            System.err.printf(ERROR_IO_CONSTRUCT, fileToWrite);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void decreaseIndent() {
        indent_level--;
    }

    public void dispose() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            System.err.println(ERROR_IO_CLOSE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void increaseIndent() {
        indent_level++;
    }

    public void writeToFile(){
        try {
            fileWriter.write(strBuilder.toString());
        } catch (IOException e) {
            System.err.println(ERROR_IO_WRITE);
            e.printStackTrace();
        }
    }

    public void writeAssembly(String template, String ... params) {
        for (int i=0; i < indent_level; i++) {
            strBuilder.append(SEPARATOR);
        }

        strBuilder.append(String.format(template, (Object[])params));
    }

    void writeGlobalNonInit(String id, boolean flag){
        increaseIndent();

        section(AssemlyString.BSS);
        align("4");

        if(!flag){
            global(id);
        }

        decreaseIndent();
        writeAssembly(AssemlyString.VAR_NAME, id);
        increaseIndent();
        writeAssembly(AssemlyString.SKIP, "4");
        next();
        next();
        section(AssemlyString.TEXT);
        align("4");
        next();
        decreaseIndent();
    }

    void writeGlobalInit(STO expr, String id, Type t, boolean flag){
        increaseIndent();

        section(AssemlyString.DATA);
        align("4");
        if(!flag){
            global(id);
        }
        decreaseIndent();
        writeAssembly(AssemlyString.VAR_NAME, id);
        increaseIndent();

        if(t instanceof IntType){
            writeAssembly(AssemlyString.WORD, String.valueOf(((ConstSTO) expr).getIntValue()));
        }
        else if(t instanceof FloatType){
            writeAssembly(AssemlyString.SINGLE, String.valueOf(((ConstSTO) expr).getFloatValue()));
        }
        else if(t instanceof BoolType){
            writeAssembly(AssemlyString.WORD, String.valueOf(((ConstSTO) expr).getValue()));
        }

        next();
        section(AssemlyString.TEXT);
        align("4");
        next();

        decreaseIndent();
    }

    //----------------------------------------------------------------
    // HELPER FUNCTIONS
    //----------------------------------------------------------------
    void next(){
        writeAssembly(AssemlyString.nextLine);
    }

    void section(String sectionType){
        writeAssembly(AssemlyString.SECTION + sectionType);
    }

    void align(String num){
        writeAssembly(AssemlyString.ALIGN, num);
    }

    void global(String id){
        writeAssembly(AssemlyString.GLOBAL, id);
    }

    void retRestore(){
        nop();
        writeAssembly(AssemlyString.RET);
        writeAssembly(AssemlyString.RESTORE);
        next();
    }

    void asciz(String str){
        writeAssembly(AssemlyString.ASCIZ, str);
    }

    void save(String p1, String p2, String p3){
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.SAVE, p1, p2, p3);
    }

    void set(String p1, String p2){
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + SEPARATOR, p1, p2);
    }

    void cmp(String p1, String p2){
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.CMP, p1, p2);
    }

    void add(String p1, String p2, String p3){
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD, p1, p2, p3);
    }

    void nop(){
        writeAssembly(AssemlyString.NOP);
    }

    void call(String func){
        writeAssembly(AssemlyString.CALL, func, AssemlyString.nextLine);
    }

    // TODO
    void be(String branch){
        writeAssembly(AssemlyString.BE, branch);
    }

    //----------------------------------------------------------------
    // END OF HELPER FUNCTIONS
    //----------------------------------------------------------------

    void writeRodata(){
        increaseIndent();
        section(AssemlyString.RODATA);
        align("4");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.INTFMT);
        increaseIndent();
        asciz("\"%d\"");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.STRFMT);
        increaseIndent();
        asciz("\"%s\"");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.STRTF);
        increaseIndent();
        asciz("\"false\\0\\0\\0true\"");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.STRENDL);
        increaseIndent();
        asciz("\"\\n\"");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.STRARRBOUND);
        increaseIndent();
        asciz("\"Index value of %d is outside legal range [0,%d).\\n\"");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.STRNULLPTR);
        increaseIndent();
        asciz("\"Attempt to dereference NULL pointer.\\n\"");
        next();
        section(AssemlyString.TEXT);
        align("4");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.PRINTBOOL);
        increaseIndent();
        save("%sp", "-96", "%sp");
        set(AssemlyString.STRTF, "%o0");
        cmp("%g0", "%i0");
        // TODO PROBLEM!!
        writeAssembly(AssemlyString.BE, AssemlyString.FUNCTIONCALL, AssemlyString.PRINTBOOL2);
        nop();
        add("%o0", "8", "%o0");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.PRINTBOOL2);
        increaseIndent();
        call(AssemlyString.PRINTF);
        retRestore();
        decreaseIndent();

    }


    //////////////////////////// DAISY STUFF ////////////////////////////
    //----------------------------------------------------------------
    // Phase 1 Check 2
    //----------------------------------------------------------------
    private int strCount = 0;
    private int floatCount = 0;
    public void writeEndlCout()
    {
        writeAssembly(AssemlyString.COUT_COMMENT, "endl");
        String temp = AssemlyString.SET + "\t\t\t" + AssemlyString.PREFIX + AssemlyString.STRENDL + ", %s\n";
        writeAssembly(temp, "%o0");
        call(AssemlyString.PRINTF);
        nop();
        next();
    }

    public void writeStringCout(String input)
    {
        section(AssemlyString.RODATA);
        align("4");
        decreaseIndent();
        writeAssembly(AssemlyString.PREFIX + AssemlyString.STR + "." +  ++strCount + ": \n");
        increaseIndent();
        asciz("\"" + input + "\"");
        next();

        section(AssemlyString.TEXT);
        align("4");
        next();
        writeAssembly(AssemlyString.COUT_COMMENT, "\"" + input + "\"");

        set(AssemlyString.PREFIX + AssemlyString.STRFMT, "%o0");
        set(AssemlyString.PREFIX + AssemlyString.STR + "." + strCount, "%o1");
        call(AssemlyString.PRINTF);
        nop();
        next();
    }

    public void writeIntLiteralCout(String name, String input)
    {
        writeAssembly(AssemlyString.COUT_COMMENT, name);
        next();
        set(input, "%o1");
        set(AssemlyString.PREFIX + AssemlyString.INTFMT, "%o0");
        call(AssemlyString.PRINTF);
        nop();
        next();
    }

    public void writeFloatLiteralCout(String name, String input)
    {
        writeAssembly(AssemlyString.COUT_COMMENT, name);
        section(AssemlyString.RODATA);
        align("4");
        decreaseIndent();
        writeAssembly(AssemlyString.PREFIX + AssemlyString.FLOAT + "." +  ++floatCount + ": \n");
        increaseIndent();
        writeAssembly(AssemlyString.SINGLE, input);
        next();

        section(AssemlyString.TEXT);
        align("4");
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + AssemlyString.SEPARATOR,
                AssemlyString.PREFIX + AssemlyString.FLOAT + "." + floatCount, "%l7");
        writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD, "%l7", "%f0\n");
        call(AssemlyString.PRINTFLOAT);
        nop();
        next();
    }

    public void writeBoolLiteralCout(String name, String input)
    {
        writeAssembly(AssemlyString.COUT_COMMENT, name);
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", input, "%o0");
        call(AssemlyString.PRINTBOOL);
        nop();
        next();
    }
}
