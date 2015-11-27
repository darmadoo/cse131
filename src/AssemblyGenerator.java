

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;
import java.util.Stack;

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

    // Stack registers
    private static String sp = "%sp";
    private static String fp = "%fp";

    // Global registers
    private static String g0 = "%g0";
    private static String g1 = "%g1";

    // Local registers
    private static String l0 = "%l0";
    private static String l1 = "%l1";
    private static String l2 = "%l2";
    private static String l3 = "%l3";
    private static String l4 = "%l4";
    private static String l5 = "%l5";
    private static String l6 = "%l6";
    private static String l7 = "%l7";

    // Out registers
    private static String o0 = "%o0";
    private static String o1 = "%o1";
    private static String o2 = "%o2";

    private static String i0 = "%i0";
    private static String i1 = "%i1";
    private static String i2 = "%i2";

    private static String f0 = "%f0";
    private static String f1 = "%f1";

    private static final StringBuilder strBuilder = new StringBuilder();
    private static final StringBuilder buffer = new StringBuilder();
    private static Stack<String> structStack = new Stack<>();

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

    public void writeGlobalNonInit(STO sto, boolean flag){
        increaseIndent();

        section(AssemlyString.BSS);
        align("4");

        if(!flag){
            global(sto.getName());
        }

        decreaseIndent();
        writeAssembly(AssemlyString.VAR_NAME, sto.getOffset());
        increaseIndent();
        writeAssembly(AssemlyString.SKIP, Integer.toString(sto.getType().getSize()));
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
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.CMP + SEPARATOR, p1, p2);
    }


    private void fcmps(String p1, String p2){
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.FCMPS, p1, p2);
    }

    void add(String p1, String p2, String p3){
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + SEPARATOR, p1, p2, p3);
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
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.MOV+ SEPARATOR, p1, p2);
    }

    private void ld(String p1, String p2){
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.LD + SEPARATOR, "[" + p1 + "]", p2);
    }

    private void st(String p1, String p2){
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.ST + SEPARATOR, p1 ,"[" + p2 + "]");
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
        writeAssembly(AssemlyString.BNE + SEPARATOR + SEPARATOR + SEPARATOR + AssemlyString.PREFIX + AssemlyString.PTRCHECK2 + "\n");
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
    }

    public void initGlobal(String id, STO expr, int offset){
        writeAssembly(AssemlyString.INIT_VAR, id);
        increaseIndent();
        // TODO PASS IN FUNCTIONS INTO CALL
        set("SAVE." + ".$.init." + id , g1);
        save(sp, g1, sp);

        increaseIndent();
        next();
        set(id, o1);
        add(g0, o1, o1);
        set(expr.getOffset(), l7);
        add(expr.getBase(), l7, l7);
        ld(l7, o0);
        st(o0, o1);
        next();
        decreaseIndent();

        // TODO SAVE THEM IN THE BUFFER AND PRINT WHENEVER YOU NEED TO

        //TODO PASS FUNCTIONS
        writeAssembly("! End of function .$.init." + id );
        next();
        call(".$.init." + id + ".fini");
        nop();
        retRestore();
        // TODO USE MEM_ALLOCATE
        assign("SAVE." + ".$.init." + id, "-(92 + " + (-offset) + ") & -8");
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
    Vector<String> func = new Vector<>();
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

        if(!func.contains(id)){
            increaseIndent();
            writeAssembly(AssemlyString.GLOBAL, id);
            func.add(id);
            decreaseIndent();

            writeAssembly(id + ":");
            next();
        }

        writeAssembly(temp + ":");
        next();
        increaseIndent();
        set("SAVE." + temp, g1);
        save(sp, g1, sp);
        next();
    }

    private int fixedVal = 64;
    public void writeAllocateMem(Vector<STO> params){
        increaseIndent();
        writeAssembly(AssemlyString.STORE_PARAM);
        if(params != null){
            for(int i = 0; i < params.size(); i++){
                fixedVal += params.get(i).getType().getSize();
                if(params.get(i).getType() instanceof FloatType){
                    if(params.get(i) instanceof VarSTO && ((VarSTO) params.get(i)).getPbr()){
                        st("%i" + i, fp + "+" + fixedVal);
                    }
                    else{
                        st("%f" + i, fp + "+" + fixedVal);
                    }
                } else{
                    st("%i" + i, fp + "+" + fixedVal);
                }
                params.get(i).setBase(fp);
                params.get(i).setOffset(Integer.toString(fixedVal));
            }
        }
        next();
        decreaseIndent();
    }

    public void writeFuncDecl2(String id, Vector<STO> params, int offset, boolean isStruct, boolean isDtor){
        if(!isStruct){
            temp = id;
        }
        else{
            if(isDtor){
                temp = id + ".$" + id;
            }
            else{
                temp = id + "." + id;
            }
        }

        if(params == null){
            temp += ".void";
        }
        else{
            if(params.size() == 0){
                temp += ".void";
            }
            else{
                for(int i = 0; i < params.size(); i++){
                    temp += "." + params.get(i).getType().getName();
                }
            }
        }

        writeAssembly("! End of function " + temp);
        next();
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

        while(!structStack.empty()){
            String top = structStack.pop();
            set(top, o0);
            ld(o0,o0);
            cmp(o0,g0);
            be(top + ".fini.skip\n");
            nop();
            String structName = structStack.pop();
            call(structName + ".$" + structName + ".void");
            nop();
            set(top, o0);
            st(g0, o0);
            decreaseIndent();
            writeAssembly(top + ".fini.skip:\n");
            increaseIndent();
        }

        retRestore();
        decreaseIndent();
        next();

        fixedVal = 64;
    }

    //////////////////////////// DAISY STUFF ////////////////////////////
    //----------------------------------------------------------------
    // Phase 1 Check 2 & 3
    //----------------------------------------------------------------
    private int strCount = 0;
    private int floatCount = 0;
    private int cmpCount = 0;
    private int andorCount = 0;
    private int ifCount = 0;
    private int whileCount = 0;

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
        if(sto instanceof VarSTO){
            if(((VarSTO)sto).getisSet()){
                writeAssembly(AssemlyString.COUT_COMMENT, ((VarSTO) sto).getInsideStruct() + "." + sto.getName());
            }
            else {
                writeAssembly(AssemlyString.COUT_COMMENT, sto.getName());
            }
        }
        else{
            writeAssembly(AssemlyString.COUT_COMMENT, sto.getName());
        }

        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", sto.getOffset(), "%l7");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", sto.getBase(), "%l7", "%l7");

        if(sto instanceof VarSTO && ((VarSTO) sto).getPbr()){
            writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", l7, l7);
        }

        if(sto instanceof VarSTO){
            if(((VarSTO)sto).getisSet()){
                ld(l7,l7);
            }
        }

        if(sto.getLoad()){
            ld(l7,l7);
        }

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
        writeAssembly(AssemlyString.PREFIX + AssemlyString.FLOAT + "." + ++floatCount + ": \n");
        increaseIndent();
        writeAssembly(AssemlyString.SINGLE, input);
        next();

        section(AssemlyString.TEXT);
        align("4");
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + SEPARATOR,
                AssemlyString.PREFIX + AssemlyString.FLOAT + "." + floatCount, "%l7");
        writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%f0");
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

        if(sto instanceof VarSTO){
            if(((VarSTO) sto).getInsideStruct() != null){
                writeAssembly(AssemlyString.VAR_DECL_COMMENT, ((VarSTO) sto).getInsideStruct() + "." + sto.getName(), expr.getName());
            }
            else{
                writeAssembly(AssemlyString.VAR_DECL_COMMENT, sto.getName(), expr.getName());
            }
        }
        else{
            writeAssembly(AssemlyString.VAR_DECL_COMMENT, sto.getName(), expr.getName());
        }

        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", sto.getOffset(), "%o1");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", sto.getBase(), "%o1", "%o1");

        if(sto instanceof VarSTO){
            if(((VarSTO)sto).getPbr() || ((VarSTO) sto).getInsideStruct() != null){
                ld(o1,o1);
            }
        }

        if(sto.getLoad()){
            ld(o1,o1);
        }

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
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + SEPARATOR,
                    AssemlyString.PREFIX + AssemlyString.FLOAT + "." + floatCount, "%l7");
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%f0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f0", "%o1");
        }
        else if(sto.getType() instanceof FloatType && expr.getType() instanceof  IntType)
        {
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", value, "%o0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", expr.getOffset(), "%l7");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", expr.getBase(), "%l7", "%l7");
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

    public void writeLocalInitWithVar(STO sto, STO expr, STO temp)
    {
        increaseIndent();
        writeAssembly(AssemlyString.VAR_DECL_COMMENT, sto.getName(), expr.getName());
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", sto.getOffset(), "%o1");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", sto.getBase(), "%o1", "%o1");

        if(sto instanceof VarSTO){
            if(((VarSTO)sto).getPbr()){
                ld(o1,o1);
            }
        }

        if(sto.getLoad()){
            ld(o1,o1);
        }

        if(expr instanceof VarSTO){
            if(((VarSTO)expr).getPbr()){
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", Integer.toString(fixedVal), "%l7");
            }
            else{
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", expr.getOffset(), "%l7");
            }
        }
        else{
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", expr.getOffset(), "%l7");
        }

        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", expr.getBase(), "%l7", "%l7");


        if(expr.getLoad()){
            ld(l7,l7);
        }

        if(sto.getType() instanceof FloatType && expr.getType() instanceof FloatType) {
            if(expr instanceof VarSTO){
                if(((VarSTO)expr).getPbr()){
                    writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", l7);
                }
            }
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%f0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f0", "%o1");
        }
        else if (sto.getType() instanceof IntType && expr.getType() instanceof IntType){
            if(expr instanceof VarSTO){
                if(((VarSTO)expr).getPbr()){
                    writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", l7);
                }
            }
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }
        else if (sto.getType() instanceof BoolType && expr.getType() instanceof BoolType){
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }
        else if (sto.getType() instanceof FloatType && expr.getType() instanceof IntType && temp != null)
        {
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", temp.getOffset(), "%l7");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", temp.getBase(), "%l7", "%l7");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%l7");
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%f0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.FITOS, "%f0", "%f0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f0", "%o1");
        }
        else if(sto.getType() instanceof BoolType && expr.getType() instanceof BoolType){
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }
        else if (sto.getType().isPointer() && expr.getType().isPointer())
        {
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }

        next();
        decreaseIndent();
    }

    public void writeLocalInitWithStruct(STO sto, STO expr, STO temp){
        increaseIndent();
        writeAssembly(AssemlyString.VAR_DECL_COMMENT, sto.getName(), expr.getName());
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", sto.getOffset(), o0);
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", sto.getBase(), o0, o0);
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", expr.getOffset(), o1);
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", expr.getBase(), o1, o1);

        set(Integer.toString(expr.getType().getSize()), o2);
        call(AssemlyString.MEMMOVE);
        nop();
        next();
        decreaseIndent();
    }

    public void writeLocalInitWithVar(STO sto, STO expr, STO temp, boolean flag)
    {
        increaseIndent();
        increaseIndent();

        writeAssembly(AssemlyString.VAR_DECL_COMMENT, "this." + sto.getName(), expr.getName());
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", sto.getOffset(), "%o1");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", sto.getBase(), "%o1", "%o1");

        if(sto instanceof VarSTO){
            if(((VarSTO)sto).getPbr() || flag){
                ld(o1,o1);
            }
        }

        if(sto.getLoad()){
            ld(o1,o1);
        }

        if(expr instanceof VarSTO){
            if(((VarSTO)expr).getPbr()){
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", Integer.toString(fixedVal), "%l7");
            }
            else{
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", expr.getOffset(), "%l7");
            }
        }
        else{
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", expr.getOffset(), "%l7");
        }

        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", expr.getBase(), "%l7", "%l7");


        if(expr.getLoad()){
            ld(l7,l7);
        }

        if(sto.getType() instanceof FloatType && expr.getType() instanceof FloatType) {
            if(expr instanceof VarSTO){
                if(((VarSTO)expr).getPbr()){
                    writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", l7);
                }
            }
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%f0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f0", "%o1");
        }
        else if (sto.getType() instanceof IntType && expr.getType() instanceof IntType){
            if(expr instanceof VarSTO){
                if(((VarSTO)expr).getPbr()){
                    writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", l7);
                }
            }
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }
        else if (sto.getType() instanceof BoolType && expr.getType() instanceof BoolType){
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }
        else if (sto.getType() instanceof FloatType && expr.getType() instanceof IntType && temp != null)
        {
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", temp.getOffset(), "%l7");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", temp.getBase(), "%l7", "%l7");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%l7");
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%f0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.FITOS, "%f0", "%f0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f0", "%o1");
        }
        else if(sto.getType() instanceof BoolType && expr.getType() instanceof BoolType){
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }

        next();
        decreaseIndent();
        decreaseIndent();
    }

    public void writeInitGuard(String input)
    {
        increaseIndent();

        section(AssemlyString.BSS);
        align("4");

        decreaseIndent();
        writeAssembly(AssemlyString.INIT_VAR, input);
        increaseIndent();

        writeAssembly(AssemlyString.SKIP, "4");
        next();
        next();
        section(AssemlyString.TEXT);
        align("4");
        next();

        writeAssembly("! Start init guard \n");
        writeAssembly(AssemlyString.SET + "\t\t\t" + AssemlyString.INIT, input + ", %o0\n");
        writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%o0", "%o0");
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.CMP + "\t", "%o0", "%g0");
        writeAssembly(AssemlyString.BNE + "\t\t\t" + AssemlyString.INIT, input + ".done \n");
        nop();
        next();

    }

    public void writeGlobalStaticInitWithVar(STO sto, STO expr, int offset)
    {
        //writeGlobalNonInit(sto.getOffset(), true);
        writeLocalInitWithVar(sto, expr, null);

        writeAssembly("! End of function " + AssemlyString.INIT, sto.getOffset() + "\n");
        call(".$.init." + sto.getOffset() + ".fini");
        nop();
        retRestore();
        assign("SAVE." + temp, "-(92 + " + (-offset) + ") & -8");
        next();

        decreaseIndent();

        initGlobalDone(sto.getOffset());

        next();

    }

    public void writeLocalStaticInitWithVar(STO sto, STO expr, STO temp)
    {
        writeGlobalNonInit(sto, true);

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

        writeLocalInitWithVar(sto, expr, temp);

        writeAssembly("! End init guard \n");
        writeAssembly(AssemlyString.SET + "\t\t\t" + AssemlyString.INIT, sto.getOffset() + ", %o0\n");
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.MOV + "\t", "1", "%o1");

        writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o1", "%o0");
        decreaseIndent();
        writeAssembly(AssemlyString.INIT_VAR, sto.getOffset() + ".done");
        next();
    }

    /*
    public void writeStaticGlobalInit(String input)
    {
        writeAssembly(AssemlyString.INIT_VAR, input);
        increaseIndent();
        set("SAVE." + ".$.init." + input , g1);
        save(sp, g1, sp);
        next();
    }
    */

    //----------------------------------------------------------------
    // Phase 1 Check 4, Phase 2 Check 3
    //----------------------------------------------------------------
    public STO writeIntegerBinaryArithmeticExpression(STO a, BinaryOp o, STO b, STO result)
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
        else if (o instanceof GreaterThanOp)
            operand = ">";
        else if (o instanceof LessThanOp)
            operand = "<";
        else if (o instanceof GreaterThanEqualOp)
            operand = ">=";
        else if (o instanceof LessThanEqualOp)
            operand = "<=";
        else if (o instanceof EqualOp)
            operand = "==";
        else if (o instanceof NotEqualOp)
            operand = "!=";

        increaseIndent();
        writeAssembly(AssemlyString.MATH_COMMENT, a.getName(), operand, b.getName());

        if(a.isConst() && !a.getIsAddressable())
        {
            set(Integer.toString(((ConstSTO) a).getIntValue()), "%o0");
        }
        else {
            if(a.getOffset() == null){
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", Integer.toString(fixedVal), "%l7");
            }
            else{
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", a.getOffset(), "%l7");
            }

            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", a.getBase(), "%l7", "%l7");

            if(a.getLoad()){
                ld(l7,l7);
            }

            if(a instanceof VarSTO && ((VarSTO) a).getPbr()){
                writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
            }
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
        }

        if(b.isConst() && !b.getIsAddressable())
        {
            set(Integer.toString(((ConstSTO) b).getIntValue()), "%o1");
        }
        else {
            if(b.getOffset() == null){
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", Integer.toString(fixedVal), "%l7");
            }
            else{
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", b.getOffset(), "%l7");
            }
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", b.getBase(), "%l7", "%l7");

            if(b.getLoad()){
                ld(l7,l7);
            }

            if(b instanceof VarSTO && ((VarSTO) b).getPbr()){
                writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
            }
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o1");
        }

        if(o instanceof ArithmeticOp){
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
        }
        else if (o instanceof AmpersandOp)
            writeAssembly(AssemlyString.THREE_PARAM, "and\t", "%o0", "%o1", "%o0");
        else if (o instanceof BarOp)
            writeAssembly(AssemlyString.THREE_PARAM, "or\t", "%o0", "%o1", "%o0");
        else if (o instanceof CaretOp)
            writeAssembly(AssemlyString.THREE_PARAM, "xor\t", "%o0", "%o1", "%o0");
        else if (o instanceof ComparisonOp){
            cmpCount++;
            cmp(o0, o1);
            if(o instanceof GreaterThanOp){
                writeAssembly(AssemlyString.BLE, AssemlyString.PREFIX + "cmp." + cmpCount);
            }
            else if (o instanceof LessThanOp)
            {
                writeAssembly(AssemlyString.BGE, AssemlyString.PREFIX + "cmp." + cmpCount);
            }
            else if (o instanceof GreaterThanEqualOp)
            {
                writeAssembly(AssemlyString.BL, AssemlyString.PREFIX + "cmp." + cmpCount);
            }
            else if (o instanceof LessThanEqualOp)
            {
                writeAssembly(AssemlyString.BG, AssemlyString.PREFIX + "cmp." + cmpCount);
            }
            else if (o instanceof EqualOp)
            {
                writeAssembly(AssemlyString.BNE + "\t\t\t" + AssemlyString.PREFIX + "cmp." + cmpCount);
            }
            else if (o instanceof NotEqualOp)
            {
                writeAssembly(AssemlyString.BE, AssemlyString.PREFIX + "cmp." + cmpCount);
            }
            next();
            mov(g0, o0);
            inc(o0);
            decreaseIndent();
            writeAssembly(AssemlyString.PREFIX + "cmp." + cmpCount + ":");
            next();
            increaseIndent();
        }


        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + SEPARATOR, result.getOffset(), "%o1");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + SEPARATOR, result.getBase(), "%o1", "%o1");
        writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        next();

        decreaseIndent();

        return result;
    }

    public void writeIntegerUnaryArithmeticExpression(STO a, UnaryOp o, boolean isPre, STO result)
    {
        increaseIndent();

        if(o instanceof MinusUnaryOp)
            writeAssembly("! -(" + a.getName() + ")\n");
        else if ( o instanceof PlusUnaryOp)
            writeAssembly("! +(" + a.getName() + ")\n");
        else if(o instanceof IncOp)
        {
            if(isPre)
                writeAssembly("! ++(" + a.getName() + ")\n");
            else
                writeAssembly("! (" + a.getName() + ")++\n");
        }
        else if(o instanceof DecOp)
        {
            if(isPre)
                writeAssembly("! --(" + a.getName() + ")\n");
            else
                writeAssembly("! (" + a.getName() + ")--\n");
        }

        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + SEPARATOR, a.getOffset(), "%l7");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + SEPARATOR, a.getBase(), "%l7", "%l7");

        if(a instanceof VarSTO && ((VarSTO) a).getPbr()){
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
        }

        if(a.getLoad()){
            ld(l7,l7);
        }

        writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");

        if(o instanceof MinusUnaryOp)
        {
            writeAssembly(AssemlyString.TWO_PARAM, "neg\t", "%o0", "%o0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + SEPARATOR, result.getOffset(), "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + SEPARATOR, result.getBase(), "%o1", "%o1");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }
        else if(o instanceof PlusUnaryOp)
        {
            writeAssembly(AssemlyString.TWO_PARAM, "mov\t", "%o0", "%o0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + SEPARATOR, result.getOffset(), "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + SEPARATOR, result.getBase(), "%o1", "%o1");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }
        else
        {
            if(a.getType().isPointer())
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", "4", "%o1");
            else
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", "1", "%o1");

            if(o instanceof IncOp)
                writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", "%o0", "%o1", "%o2");
            else
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

    public void writeExit(STO expr)
    {
        increaseIndent();
        writeAssembly("! exit(%s)\n", expr.getName());
        if(expr.isConst())
        {
            set(Integer.toString(((ConstSTO)expr).getIntValue()), o0);
        }
        else
        {
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", expr.getOffset(), l7);
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", expr.getBase(), l7, l7);
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
        }
        call("exit");
        nop();
        next();
        decreaseIndent();
    }

    //----------------------------------------------------------------
    // Phase 2 Check 1
    //----------------------------------------------------------------
    public void writeFloatBinaryArithmeticExpression(STO a, BinaryOp o, STO b, STO result, STO temp){
        increaseIndent();

        String operand = "";
        if(o instanceof AddOp)
            operand = "+";
        else if (o instanceof MinusOp)
            operand = "-";
        else if (o instanceof StarOp)
            operand = "*";
        else if (o instanceof SlashOp)
            operand = "/";
        else if (o instanceof GreaterThanOp)
            operand = ">";
        else if (o instanceof LessThanOp)
            operand = "<";
        else if (o instanceof GreaterThanEqualOp)
            operand = ">=";
        else if (o instanceof LessThanEqualOp)
            operand = "<=";
        else if (o instanceof EqualOp)
            operand = "==";
        else if (o instanceof NotEqualOp)
            operand = "!=";

        writeAssembly(AssemlyString.MATH_COMMENT, a.getName(), operand, b.getName());
        if(a.getType().isFloat())
        {
            //a is float literals
            if(a.isConst() && !a.getIsAddressable())
            {
                section(AssemlyString.RODATA);
                align("4");
                decreaseIndent();
                writeAssembly(AssemlyString.PREFIX + AssemlyString.FLOAT + "." +  ++floatCount + ": \n");
                increaseIndent();
                writeAssembly(AssemlyString.SINGLE, Float.toString(((ConstSTO)a).getFloatValue()));
                next();

                section(AssemlyString.TEXT);
                align("4");
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + SEPARATOR,
                        AssemlyString.PREFIX + AssemlyString.FLOAT + "." + floatCount, "%l7");
                writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%f0");
            }
            // else if a is float variables or expression
            else
            {
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", a.getOffset(), l7);
                writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", a.getBase(), l7, l7);

                if(a instanceof VarSTO && ((VarSTO) a).getPbr()){
                    writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
                }

                if(a.getLoad()){
                    ld(l7,l7);
                }

                writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%f0");
            }
        }
        // else a is int
        else if (a.getType().isInt())
        {
            // if a is int literals
            if(a.isConst() && !a.getIsAddressable())
            {
                set(Integer.toString(((ConstSTO)a).getIntValue()), o0);
            }
            else {
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", a.getOffset(), l7);
                writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", a.getBase(), l7, l7);

                if(a instanceof VarSTO && ((VarSTO) a).getPbr()){
                    writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
                }
                if(a.getLoad()){
                    ld(l7,l7);
                }

                writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
            }
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", temp.getOffset(), "%l7");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", temp.getBase(), "%l7", "%l7");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%l7");
            writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%f0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.FITOS, "%f0", "%f0");
        }

        if(b.getType().isFloat())
        {
            //a is float literals
            if(b.isConst() && !b.getIsAddressable())
            {
                section(AssemlyString.RODATA);
                align("4");
                decreaseIndent();
                writeAssembly(AssemlyString.PREFIX + AssemlyString.FLOAT + "." +  ++floatCount + ": \n");
                increaseIndent();
                writeAssembly(AssemlyString.SINGLE, Float.toString(((ConstSTO)b).getFloatValue()));
                next();

                section(AssemlyString.TEXT);
                align("4");
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + SEPARATOR,
                AssemlyString.PREFIX + AssemlyString.FLOAT + "." + floatCount, "%l7");
                writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%f1");
            }
            // else if a is float variables or expression
            else
            {
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", b.getOffset(), l7);
                writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", b.getBase(), l7, l7);

                if(b instanceof VarSTO && ((VarSTO) b).getPbr()){
                    writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
                }

                if(b.getLoad()){
                    ld(l7,l7);
                }
                writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%f1");
            }
        }
        // else a is int
        else if (b.getType().isInt())
        {
            // if a is int literals
            if(b.isConst() && !b.getIsAddressable())
            {
                set(Integer.toString(((ConstSTO)b).getIntValue()), o1);
            }
            else {
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", b.getOffset(), l7);
                writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", b.getBase(), l7, l7);

                if(b instanceof VarSTO && ((VarSTO) b).getPbr()){
                    writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
                }

                if(b.getLoad()){
                    ld(l7,l7);
                }

                writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o1");
            }
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", temp.getOffset(), "%l7");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", temp.getBase(), "%l7", "%l7");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o1", "%l7");
            writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%f1");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.FITOS, "%f1", "%f1");
        }

        if(o instanceof AddOp)
            writeAssembly(AssemlyString.THREE_PARAM, "fadds", "%f0", "%f1", "%f0");
        else if(o instanceof MinusOp)
            writeAssembly(AssemlyString.THREE_PARAM, "fsubs", "%f0", "%f1", "%f0");
        else if(o instanceof StarOp)
            writeAssembly(AssemlyString.THREE_PARAM, "fmuls", "%f0", "%f1", "%f0");
        else if(o instanceof SlashOp)
            writeAssembly(AssemlyString.THREE_PARAM, "fdivs", "%f0", "%f1", "%f0");
        else if (o instanceof ComparisonOp){
            cmpCount++;
            fcmps(f0, f1);
            nop();
            if(o instanceof GreaterThanOp){
                writeAssembly(AssemlyString.FBLE, AssemlyString.PREFIX + "cmp." + cmpCount);
            }
            else if (o instanceof LessThanOp)
            {
                writeAssembly(AssemlyString.FBGE, AssemlyString.PREFIX + "cmp." + cmpCount);
            }
            else if (o instanceof GreaterThanEqualOp)
            {
                writeAssembly(AssemlyString.FBL, AssemlyString.PREFIX + "cmp." + cmpCount);
            }
            else if (o instanceof LessThanEqualOp)
            {
                writeAssembly(AssemlyString.FBG, AssemlyString.PREFIX + "cmp." + cmpCount);
            }
            else if (o instanceof EqualOp)
            {
                writeAssembly(AssemlyString.FBNE, AssemlyString.PREFIX + "cmp." + cmpCount);
            }
            else if (o instanceof NotEqualOp)
            {
                writeAssembly(AssemlyString.FBE, AssemlyString.PREFIX + "cmp." + cmpCount);
            }
            next();
            mov(g0, o0);
            inc(o0);
            decreaseIndent();
            writeAssembly(AssemlyString.PREFIX + "cmp." + cmpCount + ":");
            next();
            increaseIndent();
        }

        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", result.getOffset(), "%o1");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", result.getBase(), "%o1", "%o1");
        if(result.getType() instanceof FloatType){
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f0", "%o1");
        }
        else {
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }

        next();
        decreaseIndent();
    }

    public void writeFloatUnaryArithmeticExpression(STO a, UnaryOp o, boolean isPre, STO result)
    {
        increaseIndent();

        if(o instanceof MinusUnaryOp)
            writeAssembly("! -" + a.getName() + "\n");
        else if ( o instanceof PlusUnaryOp)
            writeAssembly("! +" + a.getName() + "\n");
        else if(o instanceof IncOp)
        {
            if(isPre)
                writeAssembly("! ++(" + a.getName() + ")\n");
            else
                writeAssembly("! (" + a.getName() + ")++\n");
        }
        else if(o instanceof DecOp)
        {
            if(isPre)
                writeAssembly("! --(" + a.getName() + ")\n");
            else
                writeAssembly("! (" + a.getName() + ")--\n");
        }

        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", a.getOffset(), l7);
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", a.getBase(), l7, l7);

        if(a instanceof VarSTO && ((VarSTO) a).getPbr()){
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
        }

        if(a.getLoad()){
            ld(l7,l7);
        }

        writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%f0");

        if(o instanceof MinusUnaryOp)
        {
            writeAssembly(AssemlyString.TWO_PARAM, "fnegs", "%f0", "%f0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", result.getOffset(), "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", result.getBase(), "%o1", "%o1");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f0", "%o1");
        }
        else if(o instanceof PlusUnaryOp)
        {
            writeAssembly(AssemlyString.TWO_PARAM, "fmovs", "%f0", "%f0");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", result.getOffset(), "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", result.getBase(), "%o1", "%o1");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f0", "%o1");
        }
        else
        {
            next();
            section(AssemlyString.RODATA);
            align("4");
            decreaseIndent();
            writeAssembly(AssemlyString.PREFIX + AssemlyString.FLOAT + "." +  ++floatCount + ": \n");
            increaseIndent();
            writeAssembly(AssemlyString.SINGLE, "1.0");
            next();

            section(AssemlyString.TEXT);
            align("4");
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + SEPARATOR,
                    AssemlyString.PREFIX + AssemlyString.FLOAT + "." + floatCount, "%l7");
            writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%f1");

            if(o instanceof IncOp)
                writeAssembly(AssemlyString.THREE_PARAM, "fadds", "%f0", "%f1", "%f2");
            else if(o instanceof DecOp)
                writeAssembly(AssemlyString.THREE_PARAM, "fsubs", "%f0", "%f1", "%f2");

            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", result.getOffset(), "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", result.getBase(), "%o1", "%o1");

            if(isPre)
                writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f2", "%o1");
            else
                writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f0", "%o1");

            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", a.getOffset(), "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", a.getBase(), "%o1", "%o1");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%f2", "%o1");
        }

        next();
        decreaseIndent();
    }

    String shortCircuitTemp;
    public void writeShortCircuitLeft(STO a, String op)
    {
        shortCircuitTemp = a.getName();
        increaseIndent();
        writeAssembly("! Short Circuit LHS");
        next();

        if(a.isConst() && !a.getIsAddressable())
        {
            set(String.valueOf(((ConstSTO) a).getValue()), "%o0");
        }
        else {
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", a.getOffset(), "%l7");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", a.getBase(), "%l7", "%l7");

            if(a instanceof VarSTO && ((VarSTO) a).getPbr()){
                writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
            }

            if(a.getLoad()){
                ld(l7,l7);
            }

            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
        }
        cmp(o0, g0);
        andorCount++;
        if( op == "and" )
        {
            writeAssembly(AssemlyString.BE, AssemlyString.PREFIX + "andorSkip." + andorCount + "\n");
        }
        else
        {
            writeAssembly(AssemlyString.BNE + "\t\t\t" + AssemlyString.PREFIX + "andorSkip." + andorCount + "\n");
        }
        nop();
        next();
        decreaseIndent();
    }

    public void writeShortCircuitRight(STO b, String op)
    {
        increaseIndent();
        if(op == "and")
            writeAssembly("! " + shortCircuitTemp + " && " + b.getName() + "\n\n");
        else
            writeAssembly("! " + shortCircuitTemp + " || " + b.getName() + "\n\n");

        writeAssembly("! Short Circuit RHS");
        next();

        if(b.isConst() && !b.getIsAddressable())
        {
            set(String.valueOf(((ConstSTO) b).getValue()), "%o0");
        }
        else {
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", b.getOffset(), "%l7");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", b.getBase(), "%l7", "%l7");

            if(b instanceof VarSTO && ((VarSTO) b).getPbr()){
                writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
            }

            if(b.getLoad()){
                ld(l7,l7);
            }

            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
        }
        cmp(o0, g0);
        if( op == "and")
        {
            writeAssembly(AssemlyString.BE, AssemlyString.PREFIX + "andorSkip." + andorCount + "\n");
        }
        else
        {
            writeAssembly(AssemlyString.BNE + "\t\t\t" + AssemlyString.PREFIX + "andorSkip." + andorCount + "\n");
        }
        nop();
        decreaseIndent();
    }

    //----------------------------------------------------------------
    // Phase 2 Check 3
    //----------------------------------------------------------------
    public void writeBoolBinaryArithmeticExpression(STO a, BinaryOp o, STO b, STO result) {
        increaseIndent();

        String operand = "";
        if (o instanceof EqualOp)
            operand = "==";
        else if (o instanceof NotEqualOp)
            operand = "!=";
        else if (o instanceof AndOp)
            operand = "&&";
        else if (o instanceof OrOp)
            operand = "||";

        if(!( o instanceof AndOp || o instanceof OrOp))
            writeAssembly(AssemlyString.MATH_COMMENT, a.getName(), operand, b.getName());

        if(!(o instanceof AndOp || o instanceof OrOp)) {
            if (a.isConst() && !a.getIsAddressable()) {
                set(String.valueOf(((ConstSTO) a).getValue()), "%o0");
            } else {
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", a.getOffset(), "%l7");
                writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", a.getBase(), "%l7", "%l7");

                if(a instanceof VarSTO && ((VarSTO) a).getPbr()){
                    writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
                }

                if(a.getLoad()){
                    ld(l7,l7);
                }

                writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");
            }

            if (b.isConst() && !b.getIsAddressable()) {
                set(String.valueOf(((ConstSTO) b).getValue()), "%o1");
            } else {
                writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", b.getOffset(), "%l7");
                writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", b.getBase(), "%l7", "%l7");

                if(b instanceof VarSTO && ((VarSTO) b).getPbr()){
                    writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
                }

                if(b.getLoad()){
                    ld(l7,l7);
                }

                writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o1");
            }
        }

        if(o instanceof AndOp || o instanceof OrOp) {
            writeAssembly(AssemlyString.BA, AssemlyString.PREFIX + "andorEnd." + andorCount + "\n");
            String temp = "";
            if(result != null)
                temp = o0;
            else
                temp = g0;
            if(o instanceof AndOp)
            {
                mov("1", temp);
                decreaseIndent();
                writeAssembly(AssemlyString.PREFIX + "andorSkip." + andorCount + ":\n");
                increaseIndent();
                mov("0", temp);
                decreaseIndent();
                writeAssembly(AssemlyString.PREFIX + "andorEnd." + andorCount + ":\n");
                increaseIndent();
            }
            else
            {
                mov("0", temp);
                decreaseIndent();
                writeAssembly(AssemlyString.PREFIX + "andorSkip." + andorCount + ":\n");
                increaseIndent();
                mov("1", temp);
                decreaseIndent();
                writeAssembly(AssemlyString.PREFIX + "andorEnd." + andorCount + ":\n");
                increaseIndent();
            }
        }

        if (o instanceof EqualOp)
        {
            cmpCount++;
            cmp(o0, o1);
            writeAssembly(AssemlyString.BNE + "\t\t\t" + AssemlyString.PREFIX + "cmp." + cmpCount);
            next();
            mov(g0, o0);
            inc(o0);
            decreaseIndent();
            writeAssembly(AssemlyString.PREFIX + "cmp." + cmpCount + ":");
            next();
            increaseIndent();
        }
        else if (o instanceof NotEqualOp)
        {
            cmpCount++;
            cmp(o0, o1);
            writeAssembly(AssemlyString.BE, AssemlyString.PREFIX + "cmp." + cmpCount);
            next();
            mov(g0, o0);
            inc(o0);
            decreaseIndent();
            writeAssembly(AssemlyString.PREFIX + "cmp." + cmpCount + ":");
            next();
            increaseIndent();
        }

        if(result != null) {
            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", result.getOffset(), "%o1");
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", result.getBase(), "%o1", "%o1");
            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        }

        next();
        decreaseIndent();
    }

    public void writeBoolUnaryArithmeticExpression(STO a, UnaryOp o, STO result) {
        increaseIndent();

        if (o instanceof NotOp)
        {
            writeAssembly("! !" + a.getName() + "\n");
        }

        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", a.getOffset(), "%l7");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", a.getBase(), "%l7", "%l7");

        if(a instanceof VarSTO && ((VarSTO) a).getPbr()){
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
        }

        if(a.getLoad()){
            ld(l7,l7);
        }

        writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", "%l7", "%o0");

        if (o instanceof NotOp)
        {
            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.XOR + "\t", o0, "1", o0);
        }
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", result.getOffset(), "%o1");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", result.getBase(), "%o1", "%o1");
        writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");

        next();
        decreaseIndent();
    }

    //----------------------------------------------------------------
    // Phase 2 Check 4
    //----------------------------------------------------------------
    public void writeCin(STO sto)
    {
        increaseIndent();
        writeAssembly("! cin >> " + sto.getName());
        next();

        if(sto.getType().isInt())
        {
            call("inputInt");
        }
        else if(sto.getType().isFloat())
        {
            call("inputFloat");
        }
        nop();
        writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", sto.getOffset(), "%o1");
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", sto.getBase(), "%o1", "%o1");

        if(sto instanceof VarSTO && ((VarSTO) sto).getPbr()){
            writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", o1, o1);
        }

        if(sto.getLoad()){
            ld(o1, o1);
        }

        writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%o1");
        next();
        decreaseIndent();
    }

    public void changeIfCountValue(int input)
    {
        ifCount = input;
    }

    public void changeWhileCountValue(int input)
    {
        whileCount = input;
    }

    public void writeWhile()
    {
        increaseIndent();
        writeAssembly("! while ( ... ) \n");
        decreaseIndent();
        writeAssembly(AssemlyString.PREFIX + "loopCheck." + whileCount + ":\n");
        next();
        increaseIndent();
    }

    public void writeWhileLoopCondition(STO sto)
    {
        increaseIndent();
        writeAssembly("! Check loop condition \n");
        if(!sto.getIsAddressable() && sto.isConst())
        {
            set(String.valueOf(((ConstSTO) sto).getValue()), "%o0");
        }
        else {
            set(sto.getOffset(), l7);
            add(sto.getBase(), l7, l7);
            if(sto instanceof VarSTO && ((VarSTO) sto).getPbr()){
                writeAssembly(AssemlyString.LD + "\t\t\t" + AssemlyString.LOAD + "\n", l7, l7);
            }

            if(sto.getLoad()){
                ld(l7,l7);
            }

            ld(l7, o0);
        }
        cmp(o0, g0);
        writeAssembly(AssemlyString.BE, AssemlyString.PREFIX + "loopEnd." + whileCount);
        next();
        nop();
        next();
        writeAssembly("! Start of loop body \n");
        next();
    }

    public void writeEndWhileLoop()
    {
        writeAssembly("! End of loop body \n");
        writeAssembly(AssemlyString.BA, AssemlyString.PREFIX + "loopCheck." + whileCount);
        next();
        nop();
        decreaseIndent();
        writeAssembly(AssemlyString.PREFIX + "loopEnd." + whileCount + ":");
        next();
        next();
        decreaseIndent();
        whileCount--;
    }

    public void writeArrayInit(STO sto, boolean isStatic)
    {
        increaseIndent();
        section(AssemlyString.BSS);
        align("4");
        if(!isStatic)
        {
            global(sto.getName());
        }
        decreaseIndent();
        writeAssembly(AssemlyString.VAR_NAME, sto.getName());
        increaseIndent();
        writeAssembly(AssemlyString.SKIP, Integer.toString(sto.getType().getSize()));
        next();
        next();
        section(AssemlyString.TEXT);
        align("4");
        next();
        decreaseIndent();
    }

    public void writeArrayIndexCheck(STO des, STO expr, ArrayType array, STO sto)
    {
        increaseIndent();
        writeAssembly("! " + des.getName() + "[" + expr.getName() + "]");
        next();

        // if a is int literals
        if(expr.isConst() && !expr.getIsAddressable())
        {
            set(Integer.toString(((ConstSTO)expr).getIntValue()), o0);
        }
        else {
            set(expr.getOffset(), l7);
            add(expr.getBase(), l7, l7);
            if(expr.getLoad()){
                ld(l7,l7);
            }
            ld(l7,o0);
        }
        set(Integer.toString(array.getDimensions()), o1);
        call(AssemlyString.PREFIX + AssemlyString.ARRCHECK);
        nop();
        set("4", o1);
        call(".mul");
        nop();
        mov(o0,o1);
        set(des.getOffset(), o0);
        add(des.getBase(), o0, o0);
        call(AssemlyString.PREFIX + AssemlyString.PTRCHECK);
        nop();
        add(o0, o1, o0);
        set(sto.getOffset(), o1);
        add(sto.getBase(), o1, o1);
        st(o0, o1);
        next();
        decreaseIndent();
    }

    //----------------------------------------------------------------
    // Phase 2 Check 11
    //----------------------------------------------------------------
    public void writeForeach(STO sto, STO expr, STO temp, String rtType)
    {
        increaseIndent();
        writeAssembly("! foreach (...) \n");
        writeAssembly("! traversal ptr = --array\n");

        set(expr.getOffset(), o0);
        add(expr.getBase(), o0, o0);
        set("4", o1);
        writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.SUB + "\t", o0, o1, o0);

        set(temp.getOffset(), o1);
        add(temp.getBase(), o1, o1);
        st(o0, o1);

        decreaseIndent();
        writeAssembly(AssemlyString.PREFIX + "loopCheck." + whileCount + ":\n");
        increaseIndent();
        increaseIndent();

        writeAssembly("! ++traversal ptr\n");
        set(temp.getOffset(), o1);
        add(temp.getBase(), o1, o1);
        ld(o1, o0);
        set("4", o2);
        add(o0, o2, o0);
        st(o0, o1);

        writeAssembly("! traversal ptr < array end addr?\n");
        set(expr.getOffset(), o0);
        add(expr.getBase(), o0, o0);
        set(Integer.toString(expr.getType().getSize()), o1);
        add(o0, o1, o1);
        set(temp.getOffset(), o0);
        add(temp.getBase(), o0, o0);
        ld(o0, o0);
        cmp(o0, o1);
        writeAssembly(AssemlyString.BGE, AssemlyString.PREFIX + "loopEnd." + whileCount + "\n");
        nop();

        ArrayType tempArray = (ArrayType) expr.getType();
        Type next = tempArray.next();


        writeAssembly("! iterVal = currentElem\n");
        set(sto.getOffset(), o1);
        add(sto.getBase(), o1, o1);
        if(rtType != "&") {
            if (sto.getType().isInt() || sto.getType().isBool()) {
                ld(o0, o0);
                st(o0, o1);
            } else if (sto.getType().isFloat()) {
                ld(o0, f0);
                if (next.isFloat()) {
                    st(f0, o1);
                } else {
                    writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.FITOS, f0, f0);
                    st(f0, o1);
                }
            }
        }
        else
        {
            st(o0, o1);
        }

        next();

        writeAssembly("! start of loop body\n\n");

    }

    //----------------------------------------------------------------
    // Phase 3 Check 2
    //----------------------------------------------------------------
    public void writeAddressOperator(STO expr, STO sto){
        increaseIndent();
        writeAssembly("! " + sto.getName() + "\n");
        set(expr.getOffset(), o0);
        add(expr.getBase(), o0, o0);

        if(expr.getLoad()) {
            ld(o0, o0);
        }

        set(sto.getOffset(), o1);
        add(sto.getBase(), o1, o1);
        st(o0, o1);
        next();
        decreaseIndent();
    }

    //----------------------------------------------------------------
    // Phase 3 Check 1
    //----------------------------------------------------------------
    public void writePointer(STO expr, STO sto)
    {
        increaseIndent();
        writeAssembly("! " + sto.getName() + "\n");
        set(expr.getOffset(), l7);
        add(expr.getBase(), l7, l7);

        if(expr.getLoad()) {
            ld(l7, l7);
        }
        ld(l7, o0);
        call(AssemlyString.PREFIX + AssemlyString.PTRCHECK);
        nop();
        set(sto.getOffset(), o1);
        add(sto.getBase(), o1, o1);
        st(o0, o1);
        next();
        decreaseIndent();
    }

    //----------------------------------------------------------------
    // Phase 3 Check 3
    //----------------------------------------------------------------
    public void writePointerComparison(STO a, BinaryOp o , STO b , STO result)
    {
        increaseIndent();
        String operator = "";
        if(o instanceof EqualOp)
        {
            operator = " == ";
        }
        else if (o instanceof NotEqualOp)
        {
            operator = " != ";
        }

        writeAssembly("! " + a.getName() + operator + b.getName() + "\n");

        if(a.getType().isNullPointer())
        {
            set("0" , o0);
        }
        else
        {
            set(a.getOffset(), l7);
            add(a.getBase(), l7, l7);

            if(a.getLoad()) {
                ld(l7, l7);
            }
            ld(l7, o0);
        }
        if(b.getType().isNullPointer())
        {
            set("0" , o1);
        }
        else
        {
            set(b.getOffset(), l7);
            add(b.getBase(), l7, l7);

            if(b.getLoad()) {
                ld(l7, l7);
            }
            ld(l7, o1);
        }

        cmp(o0, o1);
        if(o instanceof EqualOp)
        {
            writeAssembly(AssemlyString.BNE + "\t\t\t" + AssemlyString.PREFIX + "cmp." + ++cmpCount + "\n");
        }
        else if (o instanceof NotEqualOp)
        {
            writeAssembly(AssemlyString.BE, AssemlyString.PREFIX + "cmp." + ++cmpCount + "\n");
        }
        mov(g0, o0);
        inc(o0);
        decreaseIndent();
        writeAssembly(AssemlyString.FUNCTIONCALL, "cmp." + cmpCount);
        increaseIndent();
        set(result.getOffset(), o1);
        add(result.getBase(), o1, o1);
        st(o0, o1);

        next();
        decreaseIndent();
    }


    //----------------------------------------------------------------
    // Phase 3 Check 4
    //----------------------------------------------------------------
    public void writeNewNonStructStatement(STO sto)
    {
        increaseIndent();
        writeAssembly("! new ( " + sto.getName() + " )\n");
        mov("1", o0);
        set("4", o1);
        call("calloc");
        nop();
        set(sto.getOffset(), o1);
        add(sto.getBase(), o1 , o1);
        st(o0, o1);
        next();
        decreaseIndent();
    }

    //----------------------------------------------------------------
    // Phase 3 Check 4
    //----------------------------------------------------------------
    public void writeDeleteStatement(STO sto)
    {
        increaseIndent();
        writeAssembly("! delete ( " + sto.getName() + " )\n");
        set(sto.getOffset(), l7);
        add(sto.getBase(), l7 , l7);
        if(sto.getLoad())
            ld(l7,l7);
        ld(l7,o0);
        call(AssemlyString.PREFIX + AssemlyString.PTRCHECK);
        nop();
        set(sto.getOffset(), l7);
        add(sto.getBase(), l7 , l7);
        if(sto.getLoad())
            ld(l7,l7);
        ld(l7,o0);
        call("free");
        nop();
        set(sto.getOffset(), o1);
        add(sto.getBase(), o1 , o1);
        st(g0, o1);
        next();
        decreaseIndent();
    }



    //////////////////////////// END OF DAISY STUFF ////////////////////////////

    public void writeIfCheck(STO expr) {
        increaseIndent();
        writeAssembly("! if %s", expr.getName());
        next();

        if(!expr.getIsAddressable() && expr.isConst())
        {
            set(String.valueOf(((ConstSTO) expr).getValue()), "%o0");
        }
        else {
            set(expr.getOffset(), l7);
            add(expr.getBase(), l7, l7);
            if(expr instanceof VarSTO && ((VarSTO) expr).getPbr()){
                ld(l7,l7);
            }
            if(expr.getLoad()){
                ld(l7, l7);
            }
            ld(l7, o0);
        }
        cmp(o0, g0);
        writeAssembly(AssemlyString.BE, AssemlyString.PREFIX + "else." + ifCount);
        next();
        nop();
        next();
    }

    public void writeElseBlock() {
        increaseIndent();
        writeAssembly(AssemlyString.BA, AssemlyString.PREFIX + "endif." + ifCount);
        next();
        nop();

        decreaseIndent();
        next();
        writeAssembly(" ! else \n");
        decreaseIndent();
        writeAssembly(AssemlyString.PREFIX + "else." + ifCount + ":");
        next();
        next();
        increaseIndent();
    }

    public void writeEndOfIf()
    {
        writeAssembly(" ! endif \n");
        decreaseIndent();
        writeAssembly(AssemlyString.PREFIX + "endif." + ifCount + ":\n\n");
        next();
    }

    public int writeReturn(STO expr, FuncSTO callingFunc, int offset){
        increaseIndent();
        if(expr != null){
            writeAssembly(AssemlyString.RETURN_COMMENT, expr.getName());
            if(expr.getType() instanceof IntType)
            {
                if(expr instanceof ConstSTO && !expr.getIsAddressable()){
                    set(expr.getName(), i0);
                }
                else{
                    set(expr.getOffset(), l7);
                    add(expr.getBase(), l7, l7);

                    if(expr.getLoad())
                        ld(l7, l7);

                    ld(l7,i0);
                }
                // If we have to return a float, type cast it
                if(callingFunc.getReturnType() instanceof FloatType){
                    offset -= expr.getType().getSize();

                    set(Integer.toString(offset), l7);
                    add(fp,l7,l7);
                    st(i0,l7);
                    ld(l7,f0);
                    writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.FITOS, f0, f0);
                }
            }
            else if(expr.getType() instanceof FloatType){
                if(expr instanceof ConstSTO && !expr.getIsAddressable()){
                    cmpCount++;
                    section(AssemlyString.RODATA);
                    align("4");
                    decreaseIndent();
                    writeAssembly(AssemlyString.PREFIX + expr.getType().getName() + "." + cmpCount + ":");
                    next();
                    writeAssembly(AssemlyString.SINGLE, String.valueOf(((ConstSTO) expr).getFloatValue()));
                    next();
                    section(AssemlyString.TEXT);
                    align("4");
                    set(AssemlyString.PREFIX + expr.getType().getName() + "." + cmpCount, l7);
                }
                else{
                    set(expr.getOffset(), l7);
                    add(fp,l7,l7);

                    if(expr.getLoad())
                        ld(l7, l7);
                }
                ld(l7, f0);
            }
            else if(expr.getType() instanceof BoolType){
                if(expr instanceof ConstSTO && !expr.getIsAddressable()){
                    set(String.valueOf(((ConstSTO) expr).getValue()), i0);
                }
                else{
                    set(expr.getOffset(), l7);
                    add(expr.getBase(), l7, l7);

                    if(expr.getLoad())
                        ld(l7, l7);

                    ld(l7, i0);
                }
            }
        } else {
            writeAssembly(AssemlyString.RETURN_NULL_COMMENT);
        }

        call(temp + ".fini");
        nop();
        retRestore();
        next();
        decreaseIndent();

        return offset;
    }

    // Check 9 Phase 2

    int writeFuncCall(STO sto, Vector<STO> args, int offset){
        increaseIndent();
        writeAssembly("! " + sto.getName() + "(...)\n");
        String temp = "";
        int fcount = 0;
        int ocount = 0;
        Vector<STO> param = ((FuncSTO)sto).getParams();
        for(int i = 0; i < args.size(); i++){
            temp = temp + "." + param.get(i).getType().getName();
            writeAssembly("! " + param.get(i).getName() + " <- " + args.get(i).getName() + "\n");
            if(args.get(i).isConst() && !args.get(i).getIsAddressable())
            {
                if (args.get(i).getType() instanceof FloatType){
                    section(AssemlyString.RODATA);
                    align("4");
                    decreaseIndent();
                    writeAssembly(AssemlyString.PREFIX + AssemlyString.FLOAT + "." +  ++cmpCount + ": \n");
                    increaseIndent();
                    writeAssembly(AssemlyString.SINGLE, args.get(i).getName());
                    next();

                    section(AssemlyString.TEXT);
                    align("4");
                    writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + SEPARATOR, AssemlyString.PREFIX + AssemlyString.FLOAT + "." + cmpCount, "%l7");
                    writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%f" + fcount);
                    fcount++;
                }
                else{
                    set(String.valueOf(((ConstSTO) args.get(i)).getValue()), "%o" + i);
                    ocount++;
                }

                if(args.get(i).getType() instanceof IntType && param.get(i).getType() instanceof FloatType){
                    if(args.get(i).getBase() == null){
                        args.get(i).setBase("%fp");
                    }

                    if(args.get(i).getOffset() == null){
                        if(!( args.get(i).getType() instanceof VoidType)){
                            offset -= args.get(i).getType().getSize();
                            args.get(i).setOffset(Integer.toString(offset));
                        }
                    }

                    writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", args.get(i).getOffset(), "%l7");
                    writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", args.get(i).getBase(), "%l7", "%l7");
                    writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o0", "%l7");
                    writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%f" + fcount);
                    writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.FITOS, "%f" + fcount, "%f" + fcount);
                    fcount++;
                }

            }
            else {
                if(args.get(i) instanceof VarSTO){
                    if(((VarSTO) param.get(i)).getPbr()){
                        set(args.get(i).getOffset(), "%o" + ocount);
                        add(fp, "%o" + ocount, "%o" + ocount);

                        if(((VarSTO) args.get(i)).getPbr()){
                            if (args.get(i).getType() instanceof FloatType) {
                                ld(o0, "%f" + fcount);
                            } else {
                                ld("%o" + ocount, "%o" + ocount);
                            }
                        }
                        ocount++;
                    }
                    else{
                        set(args.get(i).getOffset(), l7);
                        add(fp, l7, l7);
                        if (args.get(i).getType() instanceof FloatType) {
                            ld(l7, "%f" + fcount);
                        } else {
                            ld(l7, "%o" + ocount);
                        }
                        if(args.get(i).getType() instanceof IntType && param.get(i).getType() instanceof FloatType){
                            if(!( args.get(i).getType() instanceof VoidType)){
                                offset -= args.get(i).getType().getSize();
                                args.get(i).setOffset(Integer.toString(offset));
                            }

                            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.SET + "\t", args.get(i).getOffset(), "%l7");
                            writeAssembly(AssemlyString.THREE_PARAM, AssemlyString.ADD + "\t", args.get(i).getBase(), "%l7", "%l7");
                            writeAssembly(AssemlyString.ST + "\t\t\t" + AssemlyString.STORE + "\n", "%o1", "%l7");
                            writeAssembly(AssemlyString.LD + "\t\t\t" +  AssemlyString.LOAD + "\n", "%l7", "%f" + fcount);
                            writeAssembly(AssemlyString.TWO_PARAM, AssemlyString.FITOS, "%f" + fcount, "%f" + fcount);
                            fcount++;
                        }

                    }
                }
                else{
                    set(args.get(i).getOffset(), l7);
                    add(fp, l7, l7);
                    if (args.get(i).getType() instanceof FloatType) {
                        ld(l7, "%f" + fcount);
                    } else {
                        ld(l7, "%o" + ocount);
                    }
                }
            }
        }
        if (temp == ""){
            temp = ".void";
        }
        temp = sto.getName() + temp;
        call(temp);
        nop();

        if(!(((FuncSTO) sto).getReturnType() instanceof VoidType)){
            set(sto.getOffset(), o1);
            add(sto.getBase(), o1, o1);
            //ld(o0, o1);
            if(((FuncSTO) sto).getReturnType() instanceof FloatType){
                st(f0, o1);
            }
            else{
                st(o0, o1);
            }
        }

        next();

        decreaseIndent();
        return offset;
    }

    void writeContinue() {
        increaseIndent();
        writeAssembly(AssemlyString.CONTINUE);
        writeAssembly(AssemlyString.BA, ".$$.loopCheck." + whileCount + "\n");
        nop();
        next();
        decreaseIndent();
    }

    void writeBreak(){
        increaseIndent();
        writeAssembly(AssemlyString.BREAK);
        writeAssembly(AssemlyString.BA, ".$$.loopEnd." + whileCount + "\n");
        nop();
        next();
        decreaseIndent();
    }

    void writeStructhead(Vector<STO> params, String name, boolean flag){
        String temp;
        if(flag){
            temp = name + ".$" + name;
        }
        else{
            temp = name + "." + name;
        }
        if(params == null){
            temp += ".void";
        }
        else{
            if(params.size() == 0){
                temp += ".void";
            }
        }
        int tempVal = 68;
        for(int i = 0; i < params.size(); i++){
            tempVal += params.get(i).getType().getSize();
            temp += "." + params.get(i).getType().getName();
            params.get(i).setBase(fp);
            params.get(i).setOffset(Integer.toString(tempVal));
        }

        writeAssembly(temp + ":\n");
        increaseIndent();
        set("SAVE." + temp, g1);
        save(sp, g1, sp);
        next();

        increaseIndent();
        writeAssembly(AssemlyString.STORE_PARAM);
        st(i0, fp + "+" + 68);
        for(int i = 0; i < params.size(); i++){
            if(params.get(i).getType() instanceof FloatType){
                st("%f" + (i + 1), fp + "+" + params.get(i).getOffset());
            }
            else{
                st("%i" + (i + 1), fp + "+" + params.get(i).getOffset());
            }
        }
        next();

        decreaseIndent();
    }

    void writeThis(STO sto, STO right){
        increaseIndent();
        increaseIndent();

        writeAssembly("! " + sto.getName() + "." + right.getName() + "\n");
        set("68", o0);
        add(fp,o0,o0);
        ld(o0,o0);
        set("0", o1);
        add(g0,o1,o1);
        add(o0,o1,o0);
        set(right.getOffset(), o1);
        add(fp,o1,o1);
        st(o0,o1);

        decreaseIndent();
        decreaseIndent();
        next();
    }

    void defaultCtor(STO sto, int offset){
        String temp = sto.getName() + "." + sto.getName() + ".void";
        writeAssembly(temp + ":\n");
        increaseIndent();
        set("SAVE." + temp, g1);
        save(sp, g1, sp);
        next();

        increaseIndent();
        writeAssembly(AssemlyString.STORE_PARAM);
        st(i0, fp + "+" + 68);
        next();
        decreaseIndent();

        writeAssembly("! End of function " + temp);
        next();
        call(temp + ".fini");
        nop();
        retRestore();
        assign("SAVE." + temp, "-(92 + " + (-offset) + ") & -8");
        next();
        decreaseIndent();

        writeAssembly(temp + ".fini:");
        next();
        increaseIndent();
        save(sp, "-96" , sp);
        retRestore();
        decreaseIndent();
        next();
    }

    void writeDestructor(StructdefSTO sto, int offset){
        String temp = sto.getName() + ".$" + sto.getName() + ".void";
        writeAssembly(temp + ":\n");
        increaseIndent();
        set("SAVE." + temp, g1);
        save(sp, g1, sp);
        next();

        increaseIndent();
        writeAssembly(AssemlyString.STORE_PARAM);
        st(i0, fp + "+" + 68);
        next();
        decreaseIndent();

        writeAssembly("! End of function " + temp);
        next();
        call(temp + ".fini");
        nop();
        retRestore();
        assign("SAVE." + temp, "-(92 + " + (-offset) + ") & -8");
        next();
        decreaseIndent();

        writeAssembly(temp + ".fini:");
        next();
        increaseIndent();
        save(sp, "-96" , sp);
        retRestore();
        decreaseIndent();
        next();

        //TODO PLACE THIS SOMEWHERE ELSE MAYBE
        ctorCount = 1;
    }

    private int ctorCount = 1;
    void writeStructDecl(STO sto, Vector<STO> parameters, int offset){
        String structName = sto.getType().getName() + "." + sto.getType().getName();
        for(int i = 0; i < parameters.size(); i++){
            structName += "." + parameters.get(i).getName();
        }

        if(parameters == null){
            structName += ".void";
        }
        else{
            if(parameters.size() == 0){
                structName += ".void";
            }
        }

        increaseIndent();
        writeAssembly("! " + sto.getName() + "." + sto.getType().getName() + "(...)\n");
        set(sto.getOffset(), o0);
        add(fp, o0, o0);
        call(structName);
        nop();
        next();

        section(AssemlyString.BSS);
        align("4");
        decreaseIndent();
        writeAssembly(".$$.ctorDtor." + ctorCount + ":\n");
        structStack.add(sto.getType().getName());
        structStack.add(".$$.ctorDtor." + ctorCount);
        increaseIndent();
        writeAssembly(AssemlyString.SKIP, "4");
        next();
        next();
        section(AssemlyString.TEXT);
        align("4");
        next();
        set(".$$.ctorDtor." + ctorCount, o0);
        set(Integer.toString(offset), o1);
        add(fp,o1,o1);
        st(o1,o0);
        next();
        ctorCount++;

        decreaseIndent();
    }

    void writeStructVarInit(STO sto, STO id){
        increaseIndent();
        int tempInt;
        writeAssembly("! " + sto.getName() + "." + id.getName() + "\n");
        set(sto.getOffset(), o0);
        add(fp,o0,o0);
        if(!((VarSTO)id).getisSet()){
            ((VarSTO)id).setStructOffset(((VarSTO)sto).getStructOffset());
            set(Integer.toString(((VarSTO)id).getStructOffset()), o1);
            tempInt = ((VarSTO)sto).getStructOffset() + id.getType().getSize();
            ((VarSTO)sto).setStructOffset(tempInt);
        }
        else{
            set(Integer.toString(((VarSTO)id).getStructOffset()), o1);
        }
        add(g0, o1, o1);
        add(o0, o1, o0);
        set(id.getOffset(), o1);
        add(fp, o1,o1);
        st(o0, o1);
        next();

        decreaseIndent();
    }

}
