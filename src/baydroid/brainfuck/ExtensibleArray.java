package baydroid.brainfuck;



public class ExtensibleArray
    {
    private static final int INITIAL_COUNT = 128;
    private static final int BUFFER_COUNT = INITIAL_COUNT;

    private byte[] backingStore = new byte[INITIAL_COUNT];
    private boolean beenCleared = true;
    private int floor = 0;
    private int roof = 0;
    private int offset = 0;

    public void clear()
        {
        beenCleared = true;
        for (int i = floor; i < roof; i++) backingStore[i] = 0;
        floor = roof = offset = 0;
        }

    public int length()
        {
        return roof - floor;
        }

    public byte set(int index, byte b)
        {
        if (beenCleared)
            {
            beenCleared = false;
            offset = index;
            index = 0;
            roof = 1;
            }
        else
            {
            index -= offset;
            if (index < 0)
                {
                int extension = BUFFER_COUNT - index;
                if (extension < backingStore.length) extension = backingStore.length;
                int newFloor = floor + extension;
                byte[] newBackingStore = new byte[extension + backingStore.length];
                System.arraycopy(backingStore, floor, newBackingStore, newFloor, roof - floor);
                offset -= extension;
                floor = newFloor;
                roof += extension;
                index += extension;
                backingStore = newBackingStore;
                }
            else if (index >= backingStore.length)
                {
                int extension = BUFFER_COUNT + 1 + index - backingStore.length;
                if (extension < backingStore.length) extension = backingStore.length;
                byte[] newBackingStore = new byte[extension + backingStore.length];
                System.arraycopy(backingStore, floor, newBackingStore, floor, roof - floor);
                backingStore = newBackingStore;
                }
            if (index < floor)
                floor = index;
            else if (index >= roof)
                roof = index + 1;
            }
        backingStore[index] = b;
        return b;
        }

    public byte get(int index)
        {
        index -= offset;
        return floor <= index && index < roof ? backingStore[index] : 0;
        }

    public int floor()
        {
        return floor + offset;
        }

    public int roof()
        {
        return roof + offset;
        }
    }
