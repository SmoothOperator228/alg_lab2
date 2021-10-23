import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class IndexStructure {

    public class IndexNote{
        private String key;
        private String blockNum;

        public IndexNote(String key, String blockNum) {
            this.key = key;
            this.blockNum = blockNum;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getBlockNum() {
            return blockNum;
        }

        public void setBlockNum(String blockNum) {
            this.blockNum = blockNum;
        }
    }

    public class DataNote{
        private String key;
        private String value;

        public DataNote(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    public final int KEY_SIZE = 10;
    public final int DATA_SIZE = 90;

    private final int MAX_NOTES_PER_BLOCK_SIZE = 8;
    private final int BLOCK_AMOUNT_SIZE = 4;

    private final int DEFAULT_MAXIMUM_NOTES_PER_BLOCK = 500;
    private final int DEFAULT_BLOCK_AMOUNT = 10;


    private final String INDICES_FILE_NAME = "IndicesFile";
    private final String DATA_BLOCK_NAME = "DataBlock";


    private final String path;
    public int blocks;
    public int maxNotesPerBlock;

    public IndexStructure(String path) throws IOException {
        this.path = path;
        File indicesFile = new File(path + INDICES_FILE_NAME);
        if (!indicesFile.exists()) {
            createIndicesFile(indicesFile);
        } else {
            readIndicesFile(indicesFile);
        }

    }



    private void createIndicesFile(File file) throws IOException {
        file.createNewFile();
        this.maxNotesPerBlock = DEFAULT_MAXIMUM_NOTES_PER_BLOCK;
        this.blocks = DEFAULT_BLOCK_AMOUNT;
        listToIndicesFile(new LinkedList<>());
        for (int i = 1; i < blocks + 1; i++) {
            File dataBlockFile = new File(path + DATA_BLOCK_NAME + i);

            if (!dataBlockFile.exists()) {
                dataBlockFile.createNewFile();
            }
        }
    }

    private void listToIndicesFile(LinkedList<IndexNote> list) throws IOException {
        FileWriter empty = new FileWriter(path + INDICES_FILE_NAME);
        empty.write("");
        empty.close();
        try (FileWriter wr = new FileWriter(path + INDICES_FILE_NAME, true)) {
            String maxNotes = this.maxNotesPerBlock + "";
            String blocks = this.blocks + "";
            int maxNotesSpaceAmount = MAX_NOTES_PER_BLOCK_SIZE - maxNotes.length();// рахуємо к-ть пробілів для зручного читання потім
            int blocksSpaceAmount = BLOCK_AMOUNT_SIZE - blocks.length();
            maxNotes += " ".repeat(maxNotesSpaceAmount);
            blocks += " ".repeat(blocksSpaceAmount);
            wr.write(maxNotes);
            wr.write(blocks);
            int size = list.size();
            for (int i = 0; i < size; i++) {
                IndexNote note = list.removeFirst();
                String key = note.key;
                String blockNum = note.blockNum;
                int keySpaces = KEY_SIZE - key.length();
                int blockNumSpaces = BLOCK_AMOUNT_SIZE -
                        blockNum.length();
                key += " ".repeat(keySpaces);
                blockNum += " ".repeat(blockNumSpaces);
                wr.write(key);
                wr.write(blockNum);
            }
            wr.flush();
        }
    }

    private void readIndicesFile(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            int size = MAX_NOTES_PER_BLOCK_SIZE + BLOCK_AMOUNT_SIZE;
            char[] buf = new char[size];
            reader.read(buf);
            StringBuilder maxNotes = new StringBuilder();
            StringBuilder countData = new StringBuilder();
            for (int i = 0; i < MAX_NOTES_PER_BLOCK_SIZE; i++) {
                maxNotes.append(buf[i]);
            }
            for (int i = MAX_NOTES_PER_BLOCK_SIZE; i < size; i++) {
                countData.append(buf[i]);
            }
            blocks = Integer.parseInt(countData.toString().trim());
            maxNotesPerBlock = Integer.parseInt(maxNotes.toString().trim());
        }
    }

    private int search(ArrayList<DataNote> list, String key){
        int comp = 1;
        int k = log2(list.size());
        int i = (int)Math.pow(2, k);
        int cmp = list.get(i).key.compareTo(key);
        if(cmp > 0){
            int pow = k - 1;
            while(true){
                System.out.println(comp++);
                int delta = (int) Math.pow(2, pow);
                if(cmp > 0) {
                    i = i - ((delta / 2) + 1);
                }else {
                    i = i + ((delta / 2) + 1);
                }
                pow--;
                cmp = list.get(i).key.compareTo(key);
                if(cmp == 0){
                    return i;
                }
            }
        }else if(cmp < 0){
            int l = log2(list.size() - (int)Math.pow(2, k)+1);
            i = list.size() + 1 - (int)Math.pow(2, l);
            int pow = l - 1;
            while(true){
                System.out.println(comp++);
                int delta = (int)Math.pow(2, pow);
                if(cmp < 0){
                    i = i + ((delta / 2) + 1);
                }else{
                    i = i - ((delta / 2) + 1);
                }
                pow--;
                cmp = list.get(i).key.compareTo(key);
                if(cmp == 0){
                    return i;
                }
            }
        }else{
            return i;
        }

    }

    public void set(String key, String value) throws IOException {
        int blockNum = Math.abs(key.hashCode() % blocks) + 1;
        LinkedList<IndexNote> indicesList = getIndicesList();
        Iterator<IndexNote> indicesListIterator = indicesList.iterator();
        while (indicesListIterator.hasNext()) //Проверка наличия ключа, в случае нахождения, замена существующего значения
        {
            IndexNote note = indicesListIterator.next();
            String _key = note.key;
            if (_key.equals(key)) {
                int _blockNum = Integer.parseInt(note.blockNum);
                LinkedList<DataNote> dataList = getDataList(_blockNum);
                ArrayList<DataNote> dataArr = new ArrayList<>(dataList);
                int index = search(dataArr, key);
                
                dataList.get(index).value = value;
                dataListToFile(dataList, _blockNum);
                return;
            }
        }
        IndexNote note = new IndexNote(key, blockNum + "");
        indicesList.add(note);
        indicesListToFile(indicesList);
        LinkedList<DataNote> dataList = getDataList(blockNum);
        int blockNotesCount = 0;
        int notesAmount = dataList.size();
        Iterator<DataNote> dataListIterator = dataList.iterator();

        boolean added = false;
        while (dataListIterator.hasNext()) //Нахождение нужного места для вставки ключа -значения
        {
            DataNote dataNote = dataListIterator.next();
            String _key = dataNote.key;
            if (_key.compareTo(key) > 0) {
                DataNote newNote = new DataNote(key, value);
                dataList.add(blockNotesCount, newNote);
                added = true;
                break;
            }
            blockNotesCount++;
        }
        if (!added) {
            DataNote dataNote = new DataNote(key, value);
            dataList.add(dataNote);
        }
        dataListToFile(dataList, blockNum);
        if (notesAmount >= maxNotesPerBlock) {
            reindex();
        }
    }

    public void delete(String key) throws IOException {
        LinkedList<IndexNote> indicesList = getIndicesList();
        Iterator<IndexNote> iterator = indicesList.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            IndexNote note = iterator.next();
            String _key = note.key;
            if (_key.equals(key)) {
                int _blockNum = Integer.parseInt(note.blockNum);
                LinkedList<DataNote> dataList = getDataList(_blockNum);
                ArrayList<DataNote> dataArr = new ArrayList<>(dataList);
                int index = search(dataArr, key);
                dataList.remove(index);
                dataListToFile(dataList, _blockNum);
                indicesList.remove(i);
                indicesListToFile(indicesList);
                return;
            }
            i++;
        }
    }

    public String get(String key) throws IOException {
        LinkedList<IndexNote> indicesList = getIndicesList();
        Iterator<IndexNote> iterator = indicesList.iterator();
        while (iterator.hasNext()) {
            IndexNote note = iterator.next();
            String _key = note.key;

            if (_key.equals(key)) {
                int _blockNum = Integer.parseInt(note.blockNum);
                LinkedList<DataNote> dataList = getDataList(_blockNum);
                ArrayList<DataNote> dataArr = new ArrayList<>(dataList);
                int index = search(dataArr, key);
                return dataList.get(index).value;
            }
        }
        return "";
    }

    private static int log2(int N)
    {
        int result = (int)(Math.log(N) / Math.log(2));

        return result;
    }

    public LinkedList<IndexNote> getIndicesList() throws IOException {
        LinkedList<IndexNote> list = new LinkedList<>();
        try (FileReader reader = new FileReader(path + INDICES_FILE_NAME)) {
            reader.skip(MAX_NOTES_PER_BLOCK_SIZE + BLOCK_AMOUNT_SIZE);
            int size = KEY_SIZE + BLOCK_AMOUNT_SIZE;
            char[] buf = new char[size];
            while (reader.read(buf) > 0) {
                StringBuilder key = new StringBuilder();
                StringBuilder blockNum = new StringBuilder();
                for (int i = 0; i < KEY_SIZE; i++) {
                    key.append(buf[i]);
                }
                for (int i = KEY_SIZE; i < size; i++) {
                    blockNum.append(buf[i]);
                }
                IndexNote note = new IndexNote(key.toString().trim(), blockNum.toString().trim());
                list.add(note);
            }
        }
        return list;
    }

    public LinkedList<DataNote> getDataList(int blockNum) throws IOException {
        LinkedList<DataNote> list = new LinkedList<>();
        try (FileReader reader = new
                FileReader(path + DATA_BLOCK_NAME + blockNum)) {
            int size = KEY_SIZE + DATA_SIZE;
            char[] buf = new char[size];
            while (reader.read(buf) > 0) {

                StringBuilder key = new StringBuilder();
                StringBuilder value = new StringBuilder();
                for (int i = 0; i < KEY_SIZE; i++) {
                    key.append(buf[i]);
                }
                for (int i = KEY_SIZE; i < size; i++) {
                    value.append(buf[i]);
                }
                DataNote note = new DataNote(key.toString().trim(), value.toString().trim());
                list.add(note);
            }
            return list;
        }
    }

    private void indicesListToFile(LinkedList<IndexNote> list) throws IOException {
        FileWriter writer1 = new FileWriter(path + INDICES_FILE_NAME);
        writer1.write("");
        writer1.close();
        try (FileWriter writer = new FileWriter(path + INDICES_FILE_NAME, true)) {
            String maxNotes = this.maxNotesPerBlock + "";
            String amountBlocks = this.blocks + "";
            int maxNotesSpaces = MAX_NOTES_PER_BLOCK_SIZE - maxNotes.length();
            int amountBlocksSpaces = BLOCK_AMOUNT_SIZE - amountBlocks.length();
            maxNotes += " ".repeat(maxNotesSpaces);
            amountBlocks += " ".repeat(amountBlocksSpaces);
            writer.write(maxNotes);
            writer.write(amountBlocks);
            int size = list.size();
            for (int i = 0; i < size; i++) {
                IndexNote note = list.removeFirst();
                String key = note.key;
                String blockNum = note.blockNum;
                int keySpaces = KEY_SIZE - key.length();
                int blockNumSpaces = BLOCK_AMOUNT_SIZE -
                        blockNum.length();
                key += " ".repeat(keySpaces);
                blockNum += " ".repeat(blockNumSpaces);
                writer.write(key);
                writer.write(blockNum);
            }
            writer.flush();
        }
    }

    private void dataListToFile(LinkedList<DataNote> list, int blockNum) throws
            IOException{
        FileWriter writer1 = new FileWriter(path + DATA_BLOCK_NAME + blockNum);
        writer1.write("");
        writer1.close();
        try (FileWriter writer = new FileWriter(path + DATA_BLOCK_NAME + blockNum, true)) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                DataNote note = list.removeFirst();
                String key = note.key;
                String value = note.value;
                int keySpaces = KEY_SIZE - key.length();
                int blockNumSpaces = DATA_SIZE - value.length();
                key += " ".repeat(keySpaces);
                value += " ".repeat(blockNumSpaces);
                writer.write(key);
                writer.write(value);
            }
            writer.flush();
        }
    }


    private void reindex() throws IOException {
        LinkedList<IndexNote> newIndicesList = new LinkedList<>();
        for (int i = blocks + 1; i <= blocks * 2; i++) {
            File dataFile = new File(path + DATA_BLOCK_NAME + i);
            if (!dataFile.exists()) //Если дата-файла не существовало - он создаётся
            {
                dataFile.createNewFile();
            }
            LinkedList<DataNote> sourceList = getDataList(i - blocks);
            LinkedList<DataNote> destList = new LinkedList<>();
            int count = sourceList.size() / 4;
            for (int j = 0; j < count; j++) // Добавление второй половины элементов в новый блок и новый индексный файл
            {
                DataNote tempNote = sourceList.removeFirst();
                String tempVal = tempNote.value;
                String tempKey = tempNote.key;
                destList.add(tempNote);

                IndexNote newNote = new IndexNote(tempKey, i + "");
                newIndicesList.add(newNote);
            }
            dataListToFile(destList, i);
            Iterator<DataNote> iterator = sourceList.iterator();
            while (iterator.hasNext()) // добавление первой половины элементов в новый индексный файл
            {
                IndexNote note = new IndexNote(iterator.next().key, (i - blocks) + "");
                newIndicesList.add(note);
            }
            dataListToFile(sourceList, (i - blocks));
        }
        indicesListToFile(newIndicesList);
        this.blocks *= 2;
        this.maxNotesPerBlock *= 2;
    }




}
