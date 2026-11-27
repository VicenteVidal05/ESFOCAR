package com.example.myapplication

import com.example.esfocar.data.DocumentoRepository
import com.example.esfocar.data.ProfesorRepository
import com.example.esfocar.data.RubricaRepository
import com.example.esfocar.data.remote.ExternalRetrofitService
import com.example.esfocar.data.remote.dto.FeriadoExternoDto
import com.example.esfocar.model.EvaluacionRubrica
import com.example.esfocar.model.ItemRubrica
import com.example.esfocar.model.PlantillaRubrica
import com.example.esfocar.viewmodel.MenuViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profesorRepository: ProfesorRepository = mockk(relaxed = true)
    private val documentoRepository: DocumentoRepository = mockk(relaxed = true)
    private val rubricaRepository: RubricaRepository = mockk(relaxed = true)
    private val externalService: ExternalRetrofitService = mockk(relaxed = true)

    @Before
    fun setup() {
        every { profesorRepository.obtenerCorreoActivo() } returns "profe@esfocar.cl"
        every { profesorRepository.obtenerNombreActivo() } returns "Sebastián Mendoza"
        every { documentoRepository.observarPorProfesor("profe@esfocar.cl") } returns flowOf(emptyList())
        every { rubricaRepository.observarPlantillas("profe@esfocar.cl") } returns flowOf(emptyList())
        every { rubricaRepository.observarEvaluaciones("profe@esfocar.cl") } returns flowOf(emptyList())
        coEvery { rubricaRepository.sincronizarConServidor("profe@esfocar.cl") } returns true
        coEvery { externalService.obtenerProximosFeriados("CL") } returns listOf(
            FeriadoExternoDto("2026-12-08", "Inmaculada Concepción", "Immaculate Conception"),
            FeriadoExternoDto("2026-12-25", "Navidad", "Christmas Day")
        )
    }

    @Test
    fun `inicializacion carga usuario activo y feriados desde API externa`() = runTest {
        val viewModel = MenuViewModel(profesorRepository, documentoRepository, rubricaRepository, externalService)

        val state = viewModel.uiState.value
        assertEquals("profe@esfocar.cl", state.correoActivo)
        assertEquals("Sebastián Mendoza", state.nombreActivo)
        assertEquals(2, state.feriadosExternos.size)
        assertEquals("Inmaculada Concepción", state.feriadosExternos[0].nombreLocal)
    }

    @Test
    fun `guardarPlantilla con titulo vacio retorna error de validacion`() {
        val viewModel = MenuViewModel(profesorRepository, documentoRepository, rubricaRepository, externalService)
        val items = listOf(ItemRubrica(criterio = "Criterio 1"))

        viewModel.guardarPlantilla(plantillaExistente = null, titulo = "   ", items = items)

        assertEquals("Ingresa un título para la rúbrica", viewModel.uiState.value.error)
        coVerify(exactly = 0) { rubricaRepository.guardarPlantilla(any(), any()) }
    }

    @Test
    fun `guardarPlantilla valida invoca repositorio y cierra dialogo`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        coEvery { rubricaRepository.guardarPlantilla(any(), any()) } returns true

        val viewModel = MenuViewModel(
            profesorRepository = profesorRepository,
            documentoRepository = documentoRepository,
            rubricaRepository = rubricaRepository,
            externalService = externalService,
            ioDispatcher = testDispatcher
        )
        viewModel.abrirNuevaPlantilla()
        assertTrue(viewModel.uiState.value.mostrarEditorPlantilla)

        val items = listOf(ItemRubrica(criterio = "Presentación personal", puntajeLogrado = 4.0))
        viewModel.guardarPlantilla(plantillaExistente = null, titulo = "Inspección", items = items)

        advanceUntilIdle()

        assertNull("Error inesperado: ${viewModel.uiState.value.error}", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.mostrarEditorPlantilla)
        assertEquals(
            "Rúbrica guardada. Ahora puedes usarla para evaluar alumnos.",
            viewModel.uiState.value.mensaje
        )
        coVerify(exactly = 1) { rubricaRepository.guardarPlantilla(any(), false) }
    }

    @Test
    fun `eliminarPlantilla y eliminarEvaluacion delegan en el repositorio`() = runTest {
        val viewModel = MenuViewModel(profesorRepository, documentoRepository, rubricaRepository, externalService)
        val plantilla = PlantillaRubrica(id = 1, profesorCorreo = "profe@esfocar.cl", titulo = "Test", itemsJson = "[]", puntajeMaximo = 4.0)
        val evaluacion = EvaluacionRubrica(
            id = 2,
            plantillaId = 1,
            profesorCorreo = "profe@esfocar.cl",
            tituloRubrica = "Test",
            nombreEstudiante = "Alumno 1",
            itemsJson = "[]",
            puntajeObtenido = 4.0,
            puntajeMaximo = 4.0,
            notaFinal = 7.0
        )

        viewModel.eliminarPlantilla(plantilla)
        viewModel.eliminarEvaluacion(evaluacion)

        coVerify(exactly = 1) { rubricaRepository.eliminarPlantilla(plantilla) }
        coVerify(exactly = 1) { rubricaRepository.eliminarEvaluacion(evaluacion) }
    }

    @Test
    fun `sincronizarAhora manual reporta estado segun conexion`() = runTest {
        coEvery { rubricaRepository.sincronizarConServidor("profe@esfocar.cl") } returns false
        val viewModel = MenuViewModel(profesorRepository, documentoRepository, rubricaRepository, externalService)

        viewModel.sincronizarAhora(silencioso = false)

        assertEquals("Sin conexión: datos guardados localmente", viewModel.uiState.value.error)
    }

    @Test
    fun `cerrarSesion delega en profesorRepository`() {
        val viewModel = MenuViewModel(profesorRepository, documentoRepository, rubricaRepository, externalService)
        viewModel.cerrarSesion()
        verify(exactly = 1) { profesorRepository.cerrarSesion() }
    }
}