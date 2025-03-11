import java.io.*;

public class Generate extends AbstractGenerate {

    @Override
    public void reportError(Token token, String explanatoryMessage) throws CompilationException {
        String errorMessage = "Error at line " + token.lineNumber + ": " + explanatoryMessage + " (found '" + token.text + "')";
        System.out.println(errorMessage);
        throw new CompilationException(errorMessage);
    }
}
