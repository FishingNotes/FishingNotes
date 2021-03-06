package com.mobileprism.fishing

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mobileprism.fishing.viewmodels.MainViewModel
import com.mobileprism.fishing.ui.viewstates.BaseViewState
import com.mobileprism.fishing.model.entity.common.User
import com.mobileprism.fishing.model.repository.UserRepository
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var testCoroutineRule = TestCoroutineRule()

    @Mock
    private lateinit var repository: UserRepository

    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mainViewModel = MainViewModel(repository)
    }

    @Test
    fun coroutines_stateFlowNotNull() {
        testCoroutineRule.runBlockingTest {

            val stateFlow = mainViewModel.subscribe()
            assertNotNull(stateFlow.value)
        }
    }

    @Test
    fun coroutines_stateFlowHasSuccessValue() {
        testCoroutineRule.runBlockingTest {
            val user = User(uid = "1", isAnonymous = true, photoUrl = "1")

            Mockito.`when`(repository.currentUser).thenReturn(
                flow {
                    emit(user)
                }
            )

            val stateFlow = mainViewModel.subscribe()

            assertTrue(stateFlow.value is BaseViewState.Success<*>)
        }
    }

    @Test
    fun coroutines_stateFlowHasCorrectSuccessValue() {
        testCoroutineRule.runBlockingTest {
            val user = User(uid = "1", isAnonymous = true, photoUrl = "1")

            Mockito.`when`(repository.currentUser).thenReturn(
                flowOf(user)
            )

            val stateFlow = mainViewModel.subscribe()
            val viewState = stateFlow.value
            if (viewState is BaseViewState.Success<*>) {
                assertEquals(BaseViewState.Success(user), viewState)
            }
        }
    }

    @Test
    fun coroutines_repositoryCurrentUserCalledOnce() {
        testCoroutineRule.runBlockingTest {
            val user = User(uid = "1", isAnonymous = true, photoUrl = "1")

            Mockito.`when`(repository.currentUser).thenReturn(
                flowOf(user)
            )

            mainViewModel.subscribe()
            verify(repository, times(1)).currentUser
        }
    }

    @Test
    fun coroutines_stateFlowHasErrorState() {
        testCoroutineRule.runBlockingTest {

            Mockito.`when`(repository.currentUser).thenReturn(
                flow {
                    throw Exception()
                }
            )

            val stateFlow = mainViewModel.subscribe()
            val viewState = stateFlow.value
            assertTrue(viewState is BaseViewState.Error)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }


}