/**
 * Created by Darmadoo on 11/4/15.
 */
public class AssemlyString {
    public static final String SEPARATOR = "\t";

    public static final String TWO_PARAM = "%s" + SEPARATOR + SEPARATOR +  "%s, %s\n";
    public static final String THREE_PARAM = "%s" + SEPARATOR + SEPARATOR + "%s, %s, %s\n";

    public static final String nextLine = "\n";

    // Sections
    public static final String SECTION = ".section" + SEPARATOR;
    public static final String BSS = "\".bss\"\n";
    public static final String DATA = "\".data\"\n";
    public static final String TEXT = "\".text\"\n";
    public static final String RODATA = "\".rodata\"\n";
    public static final String FINI = "\".fini\"\n";
    public static final String INIT_SECTION = "\".init\"\n";

    // Align
    public static final String ALIGN = ".align" + SEPARATOR + SEPARATOR + "%s\n";

    // Variables
    public static final String VAR_NAME = "%s:\n";

    public static final String GLOBAL = ".global" + SEPARATOR + SEPARATOR + "%s\n";

    public static final String SKIP = ".skip" + SEPARATOR + SEPARATOR + "%s";

    public static final String WORD = ".word" + SEPARATOR + SEPARATOR + "%s\n";
    public static final String SINGLE = ".single" + SEPARATOR + SEPARATOR + "0r%s\n";

    public static final String PREFIX = ".$$.";

    // RODATA
    public static final String ASCIZ = ".asciz" + SEPARATOR + SEPARATOR + "%s\n";

    public static final String FUNCTIONCALL = PREFIX + "%s:\n";
    public static final String INTFMT = "intFmt";
    public static final String STRFMT = "strFmt";
    public static final String STRTF = "strTF";
    public static final String STRENDL = "strEndl";
    public static final String STRARRBOUND = "strArrBound";
    public static final String STRNULLPTR = "strNullPtr";
    public static final String PRINTBOOL = "printBool";
    public static final String PRINTBOOL2 = "printBool2";
    public static final String ARRCHECK = "arrCheck";
    public static final String ARRCHECK2 = "arrCheck2";
    public static final String PTRCHECK = "ptrCheck";
    public static final String PTRCHECK2 = "ptrCheck2";

    public static final String RET = "ret\n";
    public static final String RESTORE = "restore\n";

    public static final String INIT_VAR = ".$.init.%s:\n";
    public static final String INIT_VAR_FINI = ".$.init.%s.fini:\n";


    public static final String ASSIGN = "%s = %s\n";

    public static final String MEM_ALLOCATE = "-(92 + %s) & -8";

    public static final String SET = "set";
    public static final String SAVE = "save";
    public static final String CMP = "cmp";
    public static final String FCMPS = "fcmps";

    public static final String BE = "be" + SEPARATOR + SEPARATOR + SEPARATOR + "%s";
    public static final String BA = "ba" + SEPARATOR + SEPARATOR + SEPARATOR + "%s";
    public static final String BL = "bl" + SEPARATOR + SEPARATOR + SEPARATOR + "%s";
    public static final String BLE = "ble" + SEPARATOR + SEPARATOR + SEPARATOR + "%s";
    public static final String BGE = "bge" + SEPARATOR + SEPARATOR + SEPARATOR + "%s";
    public static final String BG = "bg" + SEPARATOR + SEPARATOR + SEPARATOR + "%s";

    public static final String NOP = "nop\n";
    public static final String CALL = "call" + SEPARATOR + SEPARATOR + "%s\n";
    public static final String PRINTF = "printf";
    public static final String EXIT = "exit";

    public static final String ADD = "add";
    public static final String LOAD = "[%s], %s";
    public  static final String INC = "inc" + SEPARATOR + SEPARATOR + SEPARATOR + "%s\n";

    //////////////////////////// DAISY STUFF ////////////////////////////
    public static final String STR = "str";
    public static final String FLOAT = "float";

    public static final String XOR = "xor";

    public static final String LD = "ld";
    public static final String ST = "st";
    public static final String BNE = "bne";
    public static final String MOV = "mov";
    public static final String FITOS = "fitos";

    public static final String FBLE = "fble" + SEPARATOR + SEPARATOR + "%s";
    public static final String FBGE = "fbge" + SEPARATOR + SEPARATOR + "%s";
    public static final String FBL = "fbl" + SEPARATOR + SEPARATOR + SEPARATOR + "%s";
    public static final String FBG = "fbg" + SEPARATOR + SEPARATOR + SEPARATOR + "%s";
    public static final String FBNE = "fbne" + SEPARATOR + SEPARATOR + "%s";
    public static final String FBE = "fbe" + SEPARATOR + SEPARATOR + SEPARATOR + "%s";

    public static final String PRINTFLOAT = "printFloat";

    public static final String COUT_COMMENT = "! cout << %s \n";
    public static final String VAR_DECL_COMMENT = "! %s = %s \n";
    public static final String MATH_COMMENT = "! (%s) %s (%s) \n";
    public static final String RETURN_COMMENT = "! return %s;\n";
    public static final String RETURN_NULL_COMMENT = "! return;\n";
    public static final String STORE_PARAM = "! Store params\n";
    public static final String CONTINUE = "!continue\n";
    public static final String BREAK = "!break\n";
    public static final String EOF_COMMENT = "! End of function %s\n";

    public static final String STORE = "%s, [%s]";
    public static final String INIT = ".$.init.%s";

    public static final String SUB = "sub";

    public static final String MEMMOVE = "memmove";

}
