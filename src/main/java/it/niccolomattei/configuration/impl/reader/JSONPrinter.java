package it.niccolomattei.configuration.impl.reader;

import it.niccolomattei.configuration.api.Printer;
import org.json.JSONObject;

public class JSONPrinter implements Printer<JSONObject, String> {

    @Override
    public String print(JSONObject input) {
        char[] chars = input.toString().toCharArray();
        StringBuilder builder = new StringBuilder();
        int spacesToAdd = 0;

        for (char ch : chars) {
            switch (ch) {
                case '{':
                case '[':
                    spacesToAdd += 2;
                    builder.append(ch);
                    builder.append("\n");
                    addSpaces(builder, spacesToAdd);
                    break;
                case ':':
                    builder.append(ch);
                    addSpaces(builder, 1);
                    break;
                case '}':
                case ']':
                    spacesToAdd -= 2;
                    builder.append("\n");
                    addSpaces(builder, spacesToAdd);
                    builder.append(ch);
                    break;
                case ',':
                    builder.append(ch);
                    builder.append("\n");
                    addSpaces(builder, spacesToAdd);
                    break;
                default:
                    builder.append(ch);
                    break;
            }
        }

        return builder.toString();
    }

    private void addSpaces(StringBuilder builder, int spaces) {
        for (; spaces > 0; spaces--) {
            builder.append(' ');
        }
    }


}
