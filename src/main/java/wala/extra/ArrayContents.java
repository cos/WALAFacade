package wala.extra;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.debug.UnimplementedError;
import com.ibm.wala.util.strings.Atom;

/***
 * 
 * Derived from ArrayContents in demandpa package
 * 
 */

public class ArrayContents implements IField {

	public static final ArrayContents theContents = new ArrayContents();

	public static final ArrayContents v() {
		return theContents;
	}

	private ArrayContents() {
	}

	@Override
	public TypeReference getFieldTypeReference() throws UnimplementedError {
		Assertions.UNREACHABLE();
		return null;
	}

	@Override
	public boolean isFinal() throws UnimplementedError {
		Assertions.UNREACHABLE();
		return false;
	}

	@Override
	public boolean isPrivate() throws UnimplementedError {
		Assertions.UNREACHABLE();
		return false;
	}

	@Override
	public boolean isProtected() throws UnimplementedError {
		Assertions.UNREACHABLE();
		return false;
	}

	@Override
	public boolean isPublic() throws UnimplementedError {
		Assertions.UNREACHABLE();
		return false;
	}

	@Override
	public boolean isStatic() throws UnimplementedError {
		Assertions.UNREACHABLE();
		return false;
	}

	@Override
	public IClass getDeclaringClass() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Atom getName() throws UnimplementedError {
		return Atom.findOrCreateUnicodeAtom("[*]");
	}

	@Override
	public String toString() {
		return "[*]";
	}

	@Override
	public boolean isVolatile() {
		return false;
	}

	@Override
	public ClassHierarchy getClassHierarchy() throws UnimplementedError {
		Assertions.UNREACHABLE();
		return null;
	}

	@Override
	public FieldReference getReference() {
		return null;
	}
}