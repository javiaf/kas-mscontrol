package com.kurento.mscontrol.kas;

public class MscontrolTest {

/*
	1. Generar una oferta formando el siguiente SDP: (generateSdpOffer)

	Video H263 Audio AMR
	 v=0
	 o=- 123456 654321 IN IP4 193.147.51.18
	 s=TestSession
	 c=IN IP4 193.147.51.18
	 t=0 0
	 m=video 39248 RTP/AVP 96
	 a=rtpmap:96 H263-1998/90000
	 a=sendrecv
	 m=audio 38394 RTP/AVP 97
	 a=rtpmap:97 AMR/8000/1
	 a=FMTP:97 octet-align=1
	 a=sendrecv


	Criterio de paso: 
		No se genera una excepcion "SdpPortManagerException".
		Se genera correctamente un "SdpPortManagerEventImpl"


	2. Usar la oferta anterior en (processSdpOffer)

	Criterio de paso:
		No se genera una excepcion "SdpPortManagerException"
		Se genera correctamente un "SdpPortManagerEventImpl"


	3. Generar una respuesta

	v=0

	 o=- 123456 654321 IN IP4 193.147.51.40
	 s=TestSession
	 c=IN IP4 193.147.51.40
	 t=0 0
	 m=video 41620 RTP/AVP 96
	 a=rtpmap:96 H263-1998/90000
	 a=sendrecv
	 m=audio 44195 RTP/AVP 97
	 a=rtpmap:97 AMR/8000/1
	 a=FMTP:97 octet-align=1
	 a=sendrecv

	 	y usarla en (processSdpAnswer)

	Criterio de paso:
		No se genera una excepcion "SdpPortManagerException"


	4. Después de la negociación:
		getMediaServerSessionDescription

	Criterio de paso:
		Puede ser null, sino se han puesto de acuerdo en el media
		Puede ser []bytes, si la negociación hay sido correcta
		Si No se genera una excepcion "SdpPortManagerException




	5. Crear un SessionSpec y usar  RTPInfo para recuperar los datos del media que se ha negociado.


	Criterio de paso:
		videoPayloadType, videoCodecType, audioPayloadType, audioCodecType NO son null
		No se genera una excepción IndexOutOfBoundsException



	6. Crear un MediaSessionConfig e intentar crear un NetworkConnectionImpl

	Criterio de paso:

		No se genera una exceptión MsControlException.
		Se crean correctamente  ArrayList<AudioProfile> y ArrayList<VideoProfile> 
		Se crean correctamente videoPort y audioPort

*/
}
