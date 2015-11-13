
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

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

    // Stack pointer
    private static String sp = "%sp";
    private static String fp = "%fp";

    // Global pointers
    private static String g0 = "%g0";
    private static String g1 = "%g1";

    // Local Pointers
    private static String l0 = "%l0";
    private static String l1 = "%l1";
    private static String l2 = "%l2";
    private static String l3 = "%l3";
    private static String l4 = "%l4";
    private static String l5 = "%l5";
    private static String l6 = "%l6";
    private static String l7 = "%l7";

    // Out pointers
    private static String o0 = "%o0";
    private static String o1 = "%o1";
    private static String o2 = "%o2";

    private static String i0 = "%i0";
    private static String i1 = "%i1";
    private static String i2 = "%i2";

    private static String f0 = "%f0";


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

    public void writeGlobalNonInit(String id, boolean flag){
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

    public void writeGlobalInit(STO expr, String id, Type t, boolean flag){
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
    private void next(){
        writeAssembly(AssemlyString.nextLine);
    }

    private void section(String sectionType){
        writeAssembly(AssemlyString.SECTION + sectionType);
    }

    private void align(String num){
        writeAssembly(AssemlyString.ALIGN, num);
    }

    private void global(String id){
        writeAssembly(AssemlyString.GLOBAL, id);
    }

    private void retRestore(){
       // nop();
        writeAssembly(AssemlyString.RET);
        writeAssembly(AssemlyString.RESTORE);
    }

    private void asciz(String str){
        writeAssembly(AssemlyString.ASCIZ, str);
    }

    private void save(String p1, String p2, String p3){
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.SAVE, p1, p2, p3);
    }

    private void set(String p1, String p2){
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + SEPARATOR, p1, p2);
    }

    private void cmp(String p1, String p2){
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.CMP, p1, p2);
    }

    void add(String p1, String p2, String p3){
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD, p1, p2, p3);
    }

    private void nop(){
        writeAssembly(AssemlyString.NOP);
    }

    private void call(String func){
        writeAssembly(AssemlyString.CALL, func, AssemlyString.nextLine);
    }

    // TODO
    private void be(String branch){
        writeAssembly(AssemlyString.BE, branch);
    }

    private void mov(String p1, String p2){
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.MOV, p1, p2);
    }

    private void ld(String p1, String p2){
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.LD, "[" + p1 + "]", p2);
    }

    private void st(String p1, String p2){
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.ST, p1 ,"[" + p2 + "]");
    }

    private void assign(String left, String right){
        writeAssembly(AssemlyString.ASSIGN, left, right);
    }

    private void inc(String str){
        writeAssembly(AssemlyString.INC, str);
    }

    //----------------------------------------------------------------
    // END OF HELPER FUNCTIONS
    //----------------------------------------------------------------

    public void writeRodata(){
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
        set(AssemlyString.PREFIX + AssemlyString.STRTF, "%o0");
        cmp("%g0", "%i0");
        // TODO PROBLEM!!
        writeAssembly(AssemlyString.BE, AssemlyString.PREFIX + AssemlyString.PRINTBOOL2 + "\n");
        nop();
        add("%o0", "8", "%o0");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.PRINTBOOL2);
        increaseIndent();
        call(AssemlyString.PRINTF);
        nop();
        retRestore();
        next();
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.ARRCHECK);
        increaseIndent();
        save("%sp", "-96", "%sp");
        cmp("%i0", "%g0");
        writeAssembly(AssemlyString.BL, AssemlyString.PREFIX + AssemlyString.ARRCHECK2 + "\n");
        nop();
        cmp("%i0", "%i1");
        writeAssembly(AssemlyString.BGE, AssemlyString.PREFIX + AssemlyString.ARRCHECK2 + "\n");
        nop();
        retRestore();
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.ARRCHECK2);
        increaseIndent();
        set(AssemlyString.PREFIX + AssemlyString.STRARRBOUND, "%o0");
        mov("%i0", "%o1");
        call(AssemlyString.PRINTF);
        mov(i1, "%o2");
        call(AssemlyString.EXIT);
        mov("1", "%o0");
        retRestore();
        next();
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.PTRCHECK);
        increaseIndent();
        save("%sp", "-96", "%sp");
        cmp("%i0", "%g0");
        writeAssembly(AssemlyString.BNE + SEPARATOR + SEPARATOR + AssemlyString.PREFIX + AssemlyString.PTRCHECK2 + "\n");
        nop();
        set(AssemlyString.PREFIX + AssemlyString.STRNULLPTR, "%o0");
        call(AssemlyString.PRINTF);
        nop();
        call(AssemlyString.EXIT);
        mov("1", "%o0");
        decreaseIndent();

        writeAssembly(AssemlyString.FUNCTIONCALL, AssemlyString.PTRCHECK2);
        increaseIndent();
        retRestore();
        decreaseIndent();

        next();
        next();
    }

    public void initGlobal(String id, STO expr){
        writeAssembly(AssemlyString.INIT_VAR, id);
        increaseIndent();
        // TODO PASS IN FUNCTIONS INTO CALL
        set("SAVE." + ".$.init." + id , g1);
        save(sp, g1, sp);

        increaseIndent();
        next();
        set(id, o1);
        add(g0, o1, o1);
        set(expr.getName(), l7);
        add(g0, l7, l7);
        ld(l7, o0);
        st(o0, o1);
        next();
        decreaseIndent();

        //TODO PASS FUNCTIONS
        call(".$.init." + id + ".fini");
        nop();
        retRestore();
        // TODO USE MEM_ALLOCATE
        assign("SAVE." + ".$.init." + id, "-(92 + 0) & -8");
        next();
        decreaseIndent();

        initGlobalDone(id);
    }

    private void initGlobalDone(String id){
        writeAssembly(AssemlyString.INIT_VAR_FINI, id);
        increaseIndent();
        save(sp, "-96", sp);
        retRestore();
        next();
        section(AssemlyString.INIT_SECTION);
        align("4");
        // TO DO FUNCTION
        call(".$.init." + id);
        nop();
        next();
        section(AssemlyString.TEXT);
        align("4");
        next();
        decreaseIndent();
    }

    // Start of function
    private String temp;
    public void writeFuncDecl(String id, Vector<STO> params){
        temp = id;
        if(params == null){
            temp += ".void";
        }
        else{
            for(int i = 0; i < params.size(); i++){
                temp += "." + params.get(i).getType().getName();
            }
        }

        increaseIndent();
        writeAssembly(AssemlyString.GLOBAL, id);
        decreaseIndent();

        writeAssembly(id + ":");
        next();
        writeAssembly(temp + ":");
        next();
        increaseIndent();
        set("SAVE." + temp, g1);
        save(sp, g1, sp);
        next();
    }

    public void writeFuncDecl2(String id, Vector<STO> params, int offset){
        temp = id;
        if(params == null){
            temp += ".void";
        }
        else{
            for(int i = 0; i < params.size(); i++){
                temp += "." + params.get(i).getType().getName();
            }
        }

        call(temp + ".fini");
        nop();
        retRestore();
        assign("SAVE." + temp, "-(92 + " + (-offset) + ") & -8");
        next();

        decreaseIndent();
        writeFuncDecl2_Done(temp);

    }

    public void writeFuncDecl2_Done(String str){
        writeAssembly(str + ".fini:");
        next();
        increaseIndent();
        save(sp, "-96" , sp);
        retRestore();
    }

    //////////////////////////// DAISY STUFF ////////////////////////////
    //----------------------------------------------------------------
    // Phase 1 Check 2 & 3
    //----------------------------------------------------------------
    private int strCount = 0;
    private int floatCount = 0;
    public void writeEndlCout()
    {
        increaseIndent();
        writeAssembly(AssemlyString.COUT_COMMENT, "endl");
        String temp = AssemlyString.SET + "\t\t\t" + AssemlyString.PREFIX + AssemlyString.STRENDL + ", %s\n";
        writeAssembly(temp, "%o0");
        call(AssemlyString.PRINTF);
        nop();
        next();
        decreaseIndent();
    }

    public void writeVarCout(STO sto)
    {
        increaseIndent();
        writeAssembly(AssemlyString.COUT_COMMENT, sto.getName());
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", sto.getOffset(), "%l7");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", sto.getBase(), "%l7", "%l7");

        if(sto.getType() instanceof IntType)
        {
            writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%o1");
            set(AssemlyString.PREFIX + AssemlyString.INTFMT, "%o0");
            call(AssemlyString.PRINTF);
            nop();
        }
        else if (sto.getType() instanceof FloatType)
        {
            writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%f0");
            call(AssemlyString.PRINTFLOAT);
            nop();
        }
        else
        {
            writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%o0");
            call(AssemlyString.PREFIX + AssemlyString.PRINTBOOL);
            nop();
        }

        next();
        decreaseIndent();
    }

    public void writeStringCout(String input)
    {
        increaseIndent();
        section(AssemlyString.RODATA);
        align("4");
        decreaseIndent();
        writeAssembly(AssemlyString.PREFIX + AssemlyString.STR + "." +  ++strCount + ": \n");
        increaseIndent();
        asciz("\"" + input + "\"");
        next();

        section(AssemlyString.TEXT);
        align("4");
        writeAssembly(AssemlyString.COUT_COMMENT, "\"" + input + "\"");

        set(AssemlyString.PREFIX + AssemlyString.STRFMT, "%o0");
        set(AssemlyString.PREFIX + AssemlyString.STR + "." + strCount, "%o1");
        call(AssemlyString.PRINTF);
        nop();
        next();
        decreaseIndent();
    }

    public void writeIntLiteralCout(String name, String input)
    {
        increaseIndent();
        writeAssembly(AssemlyString.COUT_COMMENT, name);
        next();
        set(input, "%o1");
        set(AssemlyString.PREFIX + AssemlyString.INTFMT, "%o0");
        call(AssemlyString.PRINTF);
        nop();
        next();
        decreaseIndent();
    }

    public void writeFloatLiteralCout(String name, String input)
    {
        increaseIndent();
        writeAssembly(AssemlyString.COUT_COMMENT, name);
        next();
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
        writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%f0");
        call(AssemlyString.PRINTFLOAT);
        nop();
        next();
        decreaseIndent();
    }

    public void writeBoolLiteralCout(String name, String input)
    {
        increaseIndent();
        writeAssembly(AssemlyString.COUT_COMMENT, name);
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", input, "%o0");
        call(AssemlyString.PREFIX + AssemlyString.PRINTBOOL);
        nop();
        next();
        decreaseIndent();
    }
    //----------------------------------------------------------------
    // Phase 1 Check 1
    //----------------------------------------------------------------
    public void writeLocalInitWithConst(STO sto, STO expr)
    {
        increaseIndent();
        //comment on the top
        String value = "";
        if(expr.getType() instanceof IntType){
            value =  String.valueOf(((ConstSTO) expr).getIntValue());
        }
        else if(expr.getType() instanceof FloatType){
            value = String.valueOf(((ConstSTO) expr).getFloatValue());
        }
        else if(expr.getType() instanceof BoolType){
            value =  String.valueOf(((ConstSTO) expr).getValue());
        }

        writeAssembly(AssemlyString.VAR_DECL_COMMENT, sto.getName(), expr.getName());
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", sto.getOffset(), "%o1");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", sto.getBase(), "%o1", "%o1");

        if(sto.getType() instanceof FloatType && expr.getType() instanceof  FloatType)
        {
            next();
            section(AssemlyString.RODATA);
            align("4");
            decreaseIndent();
            writeAssembly(AssemlyString.PREFIX + AssemlyString.FLOAT + "." +  ++floatCount + ": \n");
            increaseIndent();
            writeAssembly(AssemlyString.SINGLE, value);
            next();

            section(AssemlyString.TEXT);
            align("4");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + AssemlyString.SEPARATOR,
                    AssemlyString.PREFIX + AssemlyString.FLOAT + "." + floatCount, "%l7");
            writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%f0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f0", "%o1");
        }
        else if(sto.getType() instanceof FloatType && expr.getType() instanceof  IntType)
        {
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", value, "%o0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", expr.getOffset(), "%l7");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", sto.getBase(), "%l7", "%l7");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%l7");
            writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%f0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.FITOS, "%f0", "%f0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f0", "%o1");
        }
        else
        {
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", value, "%o0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }
        next();
        decreaseIndent();
    }

    public void writeLocalInitWithVar(STO sto, STO expr)
    {
        increaseIndent();
        writeAssembly(AssemlyString.VAR_DECL_COMMENT, sto.getName(), expr.getName());
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", sto.getOffset(), "%o1");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", sto.getBase(), "%o1", "%o1");

        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", expr.getOffset(), "%l7");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", expr.getBase(), "%l7", "%l7");

        if(sto.getType() instanceof FloatType) {
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%f0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f0", "%o1");
        }
        else {
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }
        next();
        decreaseIndent();
    }

    public void writeLocalStaticInitWithVar(STO sto, STO expr)
    {
        writeGlobalNonInit(sto.getOffset(), true);

        increaseIndent();

        section(AssemlyString.BSS);
        align("4");

        decreaseIndent();
        writeAssembly(AssemlyString.INIT_VAR, sto.getOffset());
        increaseIndent();

        writeAssembly(AssemlyString.SKIP, "4");
        next();
        next();
        section(AssemlyString.TEXT);
        align("4");
        next();

        writeAssembly("! Start init guard \n");
        writeAssembly(AssemlyString.SET + "\t\t\t" + AssemlyString.INIT, sto.getOffset() + ", %o0\n");
        writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%o0", "%o0");
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.CMP + "\t", "%o0", "%g0");
        writeAssembly(AssemlyString.BNE + "\t\t\t" + AssemlyString.INIT, sto.getOffset() + ".done \n");
        nop();
        next();

        decreaseIndent();
        writeLocalInitWithVar(sto, expr);
        increaseIndent();
        writeAssembly("! End init guard \n");
        writeAssembly(AssemlyString.SET + "\t\t\t" + AssemlyString.INIT, sto.getOffset() + ", %o0\n");
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.MOV + "\t", "1", "%o1");

        writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o1", "%o0");
        decreaseIndent();
        writeAssembly(AssemlyString.INIT_VAR, sto.getOffset() + ".done");
        next();
    }

    //----------------------------------------------------------------
    // Phase 1 Check 4
    //----------------------------------------------------------------
    public void writeIntegerBinaryArithmeticExpression(STO a, BinaryOp o, STO b, STO result, int count, boolean flag)
    {
        String operand = "";
        if(o instanceof AddOp)
            operand = "+";
        else if (o instanceof MinusOp)
            operand = "-";
        else if (o instanceof StarOp)
            operand = "*";
        else if (o instanceof SlashOp)
            operand = "/";
        else if (o instanceof ModOp)
            operand = "%";
        else if (o instanceof AmpersandOp)
            operand = "&";
        else if (o instanceof BarOp)
            operand = "|";
        else if (o instanceof CaretOp)
            operand = "^";
        else if(o instanceof GreaterThanOp)
            operand = ">";

        increaseIndent();
        writeAssembly(AssemlyString.MATH_COMMENT, a.getName(), operand, b.getName());

        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", a.getOffset(), "%l7");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", a.getBase(), "%l7", "%l7");
        writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", b.getOffset(), "%l7");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", b.getBase(), "%l7", "%l7");
        writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o1");

        if(o instanceof AddOp)
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", "%o0", "%o1", "%o0");
        else if(o instanceof MinusOp)
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.SUB + "\t", "%o0", "%o1", "%o0");
        else if (o instanceof StarOp)
        {
            call(".mul");
            nop();
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.MOV + "\t", "%o0", "%o0");
        }
        else if (o instanceof SlashOp)
        {
            call(".div");
            nop();
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.MOV + "\t", "%o0", "%o0");
        }
        else if (o instanceof ModOp)
        {
            call(".rem");
            nop();
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.MOV + "\t", "%o0", "%o0");
        }
        else if (o instanceof AmpersandOp)
            writeAssembly(AssemlyString.THREE_PARAM, "and\t", "%o0", "%o1", "%o0");
        else if (o instanceof BarOp)
            writeAssembly(AssemlyString.THREE_PARAM, "or\t", "%o0", "%o1", "%o0");
        else if (o instanceof CaretOp)
            writeAssembly(AssemlyString.THREE_PARAM, "xor\t", "%o0", "%o1", "%o0");
        else if(o instanceof GreaterThanOp){
            cmp(o0, o1);
            writeAssembly(AssemlyString.BLE, AssemlyString.PREFIX + "cmp." + count);
            next();
            mov(g0, o0);
            inc(o0);
            decreaseIndent();
            writeAssembly(AssemlyString.PREFIX + "cmp." + count + ":");
            next();
            increaseIndent();
        }

        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", result.getOffset(), "%o1");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", result.getBase(), "%o1", "%o1");
        writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        next();

        if(o instanceof GreaterThanOp){
            writeIfCheck(result, count);
        }

        decreaseIndent();
    }
    public void writeIntegerUnaryArithmeticExpression(STO a, UnaryOp o, boolean isPre, STO result)
    {
        increaseIndent();

        if(o instanceof MinusUnaryOp)
            writeAssembly("! -" + a.getName() + "\n");
        else if(o instanceof IncOp)
        {
            if(isPre)
                writeAssembly("! ++" + a.getName() + "\n");
            else
                writeAssembly("! " + a.getName() + "++\n");
        }
        else if(o instanceof DecOp)
        {
            if(isPre)
                writeAssembly("! --" + a.getName() + "\n");
            else
                writeAssembly("! " + a.getName() + "--\n");
        }

        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", a.getOffset(), "%l7");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", a.getBase(), "%l7", "%l7");
        writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");

        if(o instanceof MinusUnaryOp)
        {
            writeAssembly(AssemlyString.TWO_PARAM, "neg\t", "%o0", "%o0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", result.getOffset(), "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", result.getBase(), "%o1", "%o1");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }
        else if(o instanceof IncOp)
        {
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", "1", "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", "%o0", "%o1", "%o2");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", result.getOffset(), "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", result.getBase(), "%o1", "%o1");

            if(isPre)
                writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o2", "%o1");
            else
                writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");

            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", a.getOffset(), "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", a.getBase(), "%o1", "%o1");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o2", "%o1");
        }
        else if(o instanceof DecOp)
        {
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", "1", "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.SUB + "\t", "%o0", "%o1", "%o2");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", result.getOffset(), "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", result.getBase(), "%o1", "%o1");

            if(isPre)
                writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o2", "%o1");
            else
                writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");

            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", a.getOffset(), "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", a.getBase(), "%o1", "%o1");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o2", "%o1");
        }
        next();

        decreaseIndent();
    }

    public void writeIfCheck(STO expr, int count){
        set(expr.getOffset(), l7);
        add(fp, l7, l7);
        ld(l7, o0);
        cmp(o0, g0);
        writeAssembly(AssemlyString.BE, AssemlyString.PREFIX + "else." + count);
        next();
        nop();
        next();
    }

    public void writeEndofIf(int count){
        writeAssembly(AssemlyString.BA, AssemlyString.PREFIX + "else." + count);
        next();
        nop();

        decreaseIndent();
        next();
        writeAssembly(AssemlyString.PREFIX + "else." + count + ":");
        next();
        next();
    }

    public void writeReturn(STO expr, int count){
        increaseIndent();
        if(expr != null){
            writeAssembly(AssemlyString.RETURN_COMMENT, expr.getName());
            if(expr.getType() instanceof IntType)
            {
                set(expr.getName(), i0);
            }
            else if(expr.getType() instanceof FloatType){
                section(AssemlyString.RODATA);
                align("4");
                decreaseIndent();
                writeAssembly(AssemlyString.PREFIX + expr.getType().getName() + "." + count + ":");
                next();
                writeAssembly(AssemlyString.SINGLE, String.valueOf(((ConstSTO) expr).getFloatValue()));
                next();
                next();
                section(AssemlyString.TEXT);
                align("4");
                set(AssemlyString.PREFIX + expr.getType().getName() + "." + count, l7);
                ld(l7, f0);
            }
            else if(expr.getType() instanceof BoolType){
                set(String.valueOf(((ConstSTO) expr).getValue()), i0);
            }

        }
        else{
            writeAssembly(AssemlyString.RETURN_NULL_COMMENT);
        }
        call(temp + ".fini");
        nop();
        retRestore();
        next();
        decreaseIndent();

    }

}
