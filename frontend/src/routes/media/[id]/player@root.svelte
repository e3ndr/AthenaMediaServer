<script context="module">
	import * as Api from '$lib/api.mjs';

	export async function load({ fetch, params, url }) {
		try {
			const { id } = params;
			const media = await Api.getMediaById({ mediaId: id, fetch });

			const streams =
				url.searchParams.get('streams') || media.files.streams.defaultStreams.join(',');
			const quality = url.searchParams.get('quality') || 'source';

			return {
				props: {
					media,
					streams,
					quality
				}
			};
		} catch (e) {
			return {
				status: 500,
				error: new Error(e)
			};
		}
	}
</script>

<script>
	export let media;
	export let streams;
	export let quality;
</script>

<!-- TODO our own custom player -->

<!-- svelte-ignore a11y-media-has-caption -->
<video
	class="w-full h-full bg-black"
	src="{Api.PUBLIC_API_LOCATION}/media/{media.id}/stream/raw?format=MKV&quality={quality}&streams={streams}"
	onerror="console.error('Error:', this.error)"
	controls
/>
