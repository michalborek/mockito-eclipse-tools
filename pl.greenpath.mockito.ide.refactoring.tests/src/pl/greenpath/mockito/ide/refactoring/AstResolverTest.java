package pl.greenpath.mockito.ide.refactoring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.junit.Before;
import org.junit.Test;

import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;

public class AstResolverTest {

	private AstResolver testedClass;

	@Before
	public void setup() {
		testedClass = new AstResolver();
	}

	@Test
	public void shouldFindParentBodyDeclarationWhenGivenBodyDeclaration() {
		BodyDeclaration nodeMock = mock(BodyDeclaration.class);
		BodyDeclaration result = testedClass.findParentBodyDeclaration(nodeMock);

		assertThat(result, is(equalTo(nodeMock)));
	}

	@Test
	public void shouldBeNullForNull() {
		BodyDeclaration nodeMock = null;
		BodyDeclaration result = testedClass.findParentBodyDeclaration(nodeMock);
		
		assertThat(result, is(nullValue()));
	}


	
}
