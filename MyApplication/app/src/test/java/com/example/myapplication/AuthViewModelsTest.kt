package com.example.myapplication

import com.example.esfocar.data.ProfesorRepository
import com.example.esfocar.model.Profesor
import com.example.esfocar.viewmodel.LoginViewModel
import com.example.esfocar.viewmodel.RegisterViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profesorRepository: ProfesorRepository = mockk(relaxed = true)

    @Test
    fun `login con campos vacios muestra error y no llama al repositorio`() {
        val viewModel = LoginViewModel(profesorRepository)
        viewModel.cambiarCorreo("   ")
        viewModel.cambiarContrasena("")

        viewModel.iniciarSesion()

        assertEquals("Por favor, completa ambos campos", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.loginExitoso)
        coVerify(exactly = 0) { profesorRepository.iniciarSesion(any(), any()) }
    }

    @Test
    fun `login exitoso actualiza estado y normaliza correo`() = runTest {
        val profesor = Profesor("docente@esfocar.cl", "123456", "Sebastián", "Mendoza")
        coEvery { profesorRepository.iniciarSesion("docente@esfocar.cl", "123456") } returns profesor

        val viewModel = LoginViewModel(profesorRepository)
        viewModel.cambiarCorreo("  DOCENTE@ESFOCAR.CL ")
        viewModel.cambiarContrasena("123456")

        viewModel.iniciarSesion()

        assertTrue(viewModel.uiState.value.loginExitoso)
        assertNull(viewModel.uiState.value.error)
        assertEquals("¡Bienvenido/a, Sebastián!", viewModel.uiState.value.mensaje)
    }

    @Test
    fun `login con credenciales invalidas muestra mensaje de error`() = runTest {
        coEvery { profesorRepository.iniciarSesion(any(), any()) } returns null

        val viewModel = LoginViewModel(profesorRepository)
        viewModel.cambiarCorreo("docente@esfocar.cl")
        viewModel.cambiarContrasena("clave_mala")

        viewModel.iniciarSesion()

        assertFalse(viewModel.uiState.value.loginExitoso)
        assertEquals("Correo o contraseña incorrectos", viewModel.uiState.value.error)
    }

    @Test
    fun `registro exitoso marca registroExitoso en true`() = runTest {
        coEvery { profesorRepository.registrar(any()) } returns true

        val viewModel = RegisterViewModel(profesorRepository)
        viewModel.cambiarNombre("Sebastián")
        viewModel.cambiarApellidos("Mendoza")
        viewModel.cambiarCorreo("nuevo@esfocar.cl")
        viewModel.cambiarContrasena("123456")

        viewModel.registrar()

        assertTrue(viewModel.uiState.value.registroExitoso)
        assertEquals("¡Registro exitoso!", viewModel.uiState.value.mensaje)
    }

    @Test
    fun `registro con correo ya existente muestra error`() = runTest {
        coEvery { profesorRepository.registrar(any()) } returns false

        val viewModel = RegisterViewModel(profesorRepository)
        viewModel.cambiarNombre("Sebastián")
        viewModel.cambiarApellidos("Mendoza")
        viewModel.cambiarCorreo("duplicado@esfocar.cl")
        viewModel.cambiarContrasena("123456")

        viewModel.registrar()

        assertFalse(viewModel.uiState.value.registroExitoso)
        assertEquals("El correo ya está registrado", viewModel.uiState.value.error)
        assertNotNull(viewModel.uiState.value.error)
    }
}