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

        writeAssembly(AssemlyString.SECTION_BSS);
        writeAssembly(AssemlyString.ALIGN, "4");

        if(!flag){
            writeAssembly(AssemlyString.GLOBAL, id);
        }

        decreaseIndent();
        writeAssembly(AssemlyString.VAR_NAME, id);
        increaseIndent();
        writeAssembly(AssemlyString.SKIP, "4");
        writeAssembly(AssemlyString.nextLine);
        writeAssembly(AssemlyString.nextLine);
        writeAssembly(AssemlyString.SECTION_TEXT);
        writeAssembly(AssemlyString.ALIGN, "4");
        writeAssembly(AssemlyString.nextLine);

        decreaseIndent();
    }

    void writeGlobalInit(STO expr, String id, Type t, boolean flag){
        increaseIndent();

        writeAssembly(AssemlyString.SECTION_DATA);
        writeAssembly(AssemlyString.ALIGN, "4");
        if(!flag){
            writeAssembly(AssemlyString.GLOBAL, id);
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

        writeAssembly(AssemlyString.nextLine);
        writeAssembly(AssemlyString.SECTION_TEXT);
        writeAssembly(AssemlyString.ALIGN, "4");
        writeAssembly(AssemlyString.nextLine);

        decreaseIndent();
    }

    void retRestore(){
        writeAssembly(AssemlyString.NOP);
        writeAssembly(AssemlyString.RET);
        writeAssembly(AssemlyString.nextLine);
    }

    void writeRodata(){
        increaseIndent();
        writeAssembly(AssemlyString.SECTION_RODATA);
        writeAssembly(AssemlyString.ALIGN, "4");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.INTFMT);
        increaseIndent();
        writeAssembly(AssemlyString.ASCIZ, "\"%d\"");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.STRFMT);
        increaseIndent();
        writeAssembly(AssemlyString.ASCIZ, "\"%s\"");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.STRTF);
        increaseIndent();
        writeAssembly(AssemlyString.ASCIZ, "\"false\\0\\0\\0true\"");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.STRENDL);
        increaseIndent();
        writeAssembly(AssemlyString.ASCIZ, "\"\\n\"");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.STRARRBOUND);
        increaseIndent();
        writeAssembly(AssemlyString.ASCIZ, "\"Index value of %d is outside legal range [0,%d).\\n\"");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.STRNULLPTR);
        increaseIndent();
        writeAssembly(AssemlyString.ASCIZ, "\"Attempt to dereference NULL pointer.\\n\"");
        writeAssembly(AssemlyString.nextLine);
        writeAssembly(AssemlyString.SECTION_TEXT);
        writeAssembly(AssemlyString.ALIGN, "4");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.PRINTBOOL);
        increaseIndent();
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.SAVE, "%sp", "-96", "%sp");
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET, AssemlyString.STRTF, "%o0");
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.CMP, "%g0", "%i0");
        // TODO PROBLEM!!
        writeAssembly(AssemlyString.BE, AssemlyString.FUNCTIONCALL, AssemlyString.PRINTBOOL2);
        writeAssembly(AssemlyString.NOP);
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD, "%o0", "8", "%o0");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.PRINTBOOL2);
        increaseIndent();
        writeAssembly(AssemlyString.CALL, AssemlyString.PRINTF, AssemlyString.nextLine);
        retRestore();
        decreaseIndent();

    }


    ///////////////////// DAISY STUFF ////////////////////////////
    private int strCount = 0;
    public void writeEndlCout()
    {
        writeAssembly(AssemlyString.COUT_COMMENT, "endl");
        String temp = AssemlyString.SET + "\t\t\t" + AssemlyString.PREFIX + AssemlyString.STRENDL + ", %s\n";
        writeAssembly(temp, "%o0");
        writeAssembly(AssemlyString.CALL, AssemlyString.PRINTF, AssemlyString.nextLine);
        writeAssembly(AssemlyString.NOP);
        writeAssembly(AssemlyString.nextLine);
    }

    public void writeStringCout(String input)
    {
        String temp = AssemlyString.PREFIX + AssemlyString.STR + "." +  ++strCount + ": \n";
        writeAssembly(AssemlyString.SECTION_RODATA);
        writeAssembly(AssemlyString.ALIGN, "4");
        decreaseIndent();
        writeAssembly(temp);
        increaseIndent();
        writeAssembly(AssemlyString.ASCIZ, "\"" + input + "\"");
        writeAssembly(AssemlyString.nextLine);

        writeAssembly(AssemlyString.SECTION_TEXT);
        writeAssembly(AssemlyString.ALIGN, "4");
        writeAssembly(AssemlyString.nextLine);
        writeAssembly(AssemlyString.COUT_COMMENT, "\"" + input + "\"");

        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", AssemlyString.PREFIX + AssemlyString.STRFMT, "%o0");
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t",
                      AssemlyString.PREFIX + AssemlyString.STR + "." + strCount, "%o1");
        writeAssembly(AssemlyString.CALL, AssemlyString.PRINTF, AssemlyString.nextLine);
        writeAssembly(AssemlyString.NOP);
        writeAssembly(AssemlyString.nextLine);
    }

    public void writeIntLiteralCout(int input)
    {
        writeAssembly(AssemlyString.COUT_COMMENT, "Haha");
    }

}
