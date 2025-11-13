package com.example.myapplication.Home.Detail.Transcript;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.json.JSONObject;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * STT 변환(음성을 텍스트로)과 관련된 데이터와 비즈니스 로직을 처리하는 ViewModel입니다.
 * 이 ViewModel은 UI(Fragment)와 데이터 소스(네트워크, 파일) 사이의 중개자 역할을 합니다.
 */
public class TranscriptViewModel extends ViewModel {

    private static final String TAG = "TranscriptViewModel"; // 로그 출력을 위한 태그

    // 백그라운드에서 네트워크 요청과 같은 오래 걸리는 작업을 처리하기 위한 스레드 풀입니다.
    // newSingleThreadExecutor는 한 번에 하나의 작업만 순차적으로 처리하도록 보장합니다.
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // UI 상태를 관리하는 LiveData입니다.
    // _uiState는 ViewModel 내부에서만 수정 가능한 MutableLiveData로 선언하여 캡슐화를 유지합니다.
    private final MutableLiveData<UiState> _uiState = new MutableLiveData<>();
    // 외부(Fragment)에서는 값의 변경만 감지할 수 있도록 public LiveData를 제공합니다.
    public LiveData<UiState> getUiState() {
        return _uiState;
    }

    // Retrofit을 통해 생성된 API 서비스 인터페이스입니다.
    private final TranscriptApiService apiService;

    /**
     * ViewModel이 처음 생성될 때 호출되는 생성자입니다.
     * STT API와 통신하기 위한 Retrofit 클라이언트를 초기화합니다.
     */
    public TranscriptViewModel() {
        apiService = SttApiClient.getClient().create(TranscriptApiService.class);
    }

    /**
     * 오디오 파일 경로를 받아 STT 변환 요청을 시작하는 메인 메소드입니다.
     * @param audioFilePath 변환할 오디오 파일의 절대 경로
     */
    public void transcribeAudio(String audioFilePath) {
        // 1. 먼저 UI 상태를 '로딩 중'으로 변경하여 사용자에게 작업이 시작되었음을 알립니다.
        _uiState.setValue(new Loading());

        // 2. Executor를 사용하여 실제 네트워크 요청을 백그라운드 스레드에서 실행합니다.
        //    이렇게 함으로써 UI 스레드가 멈추는 현상(ANR)을 방지합니다.
        executorService.execute(() -> {
            try {
                File audioFile = new File(audioFilePath);
                if (!audioFile.exists()) {
                    _uiState.postValue(new Error("오디오 파일을 찾을 수 없습니다."));
                    return;
                }

                // 3. Retrofit과 OkHttp에서 사용할 요청(Request) 본문을 생성합니다.
                RequestBody fileBody = RequestBody.create(audioFile, MediaType.parse("audio/m4a"));
                MultipartBody.Part part = MultipartBody.Part.createFormData("audio_file", audioFile.getName(), fileBody);

                // 4. API를 동기적으로 호출합니다. 백그라운드 스레드이므로 execute()를 사용해도 안전합니다.
                Response<ResponseBody> response = apiService.transcribeAudio(part).execute();

                // 5. 서버 응답을 처리합니다.
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    String transcript = jsonObject.getString("transcribed_text");
                    _uiState.postValue(new Success(transcript)); // 성공 상태와 결과를 UI로 전달
                } else {
                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                    _uiState.postValue(new Error("서버 오류: " + response.code() + " " + errorBody)); // 실패 상태와 원인을 UI로 전달
                }
            } catch (Exception e) {
                // 6. 네트워크 오류, JSON 파싱 오류 등 모든 예외를 처리합니다.
                Log.e(TAG, "Transcription failed", e);
                _uiState.postValue(new Error("네트워크 또는 파싱 오류: " + e.getMessage()));
            }
        });
    }

    /**
     * 이 ViewModel이 더 이상 사용되지 않아 파괴될 때 호출됩니다.
     * ExecutorService를 안전하게 종료하여 메모리 누수를 방지합니다.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    // --- UI의 상태를 표현하기 위한 중첩 클래스들 (Sealed Class와 유사한 패턴) ---

    /** UI의 상태를 나타내는 기본 추상 클래스입니다. */
    public static abstract class UiState {}

    /** 데이터 로딩 중인 상태를 나타냅니다. */
    public static class Loading extends UiState {}

    /** 작업이 성공적으로 완료되어 데이터를 가지고 있는 상태를 나타냅니다. */
    public static class Success extends UiState {
        public final String transcript; // 변환된 텍스트
        public Success(String transcript) { this.transcript = transcript; }
    }

    /** 오류가 발생한 상태를 나타냅니다. */
    public static class Error extends UiState {
        public final String message; // 오류 메시지
        public Error(String message) { this.message = message; }
    }
}
