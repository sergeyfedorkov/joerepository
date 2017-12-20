package structure.objects;

import java.util.Comparator;

public class ObjectsComparator implements Comparator<GenericObject> {
	@Override
	public int compare(GenericObject o1, GenericObject o2) {
		if (o1.isDirectory() && !o2.isDirectory()) {
			return -1;
		} else if (!o1.isDirectory() && o2.isDirectory()) {
			return 1;
		} else {
			return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
		}
	} 
}
