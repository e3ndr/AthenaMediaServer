<script context="module">
	import * as Api from '$lib/api.mjs';

	export async function load({ fetch, params }) {
		try {
			const { id } = params;
			const media = await Api.getMediaById({ mediaId: id, fetch });

			return {
				props: {
					media
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
	import PageTitle from '../../../components/PageTitle.svelte';
	import AspectPoster from '../../../components/aspect-ratio/AspectPoster.svelte';

	const VIDEO_QUALITIES = ['SOURCE', 'UHD', 'FHD', 'HD', 'SD', 'LD'];

	export let media;

	let selectedVideoStream = 0;
	let selectedAudioStream = 0;
	let selectedVideoQuality = 'SOURCE';

	// Figure out the default video and video streams.
	for (const videoStream of media.files.streams.video) {
		if (media.files.streams.defaultStreams.includes(videoStream.id)) {
			selectedVideoStream = videoStream;
			break;
		}
	}
	for (const audioStream of media.files.streams.audio) {
		if (media.files.streams.defaultStreams.includes(audioStream.id)) {
			selectedAudioStream = audioStream;
			break;
		}
	}
</script>

<PageTitle title={['Media', media.info.title]} />

<div class="flex flex-col sm:flex-row">
	<div class="flex-0 w-48 mx-auto">
		<AspectPoster>
			<img class="w-full h-full object-cover rounded" alt="" src={media.files.images.posterUrl} />
		</AspectPoster>
	</div>

	<div class="mx-6 mt-4 flex-1">
		<h1 class="text-xl font-semibold">{media.info.title}</h1>
		<h2>
			<span class="text-md">{media.info.year}</span>
			<span class="ml-1 text-xs">{media.info.genres.join(', ')}</span>
		</h2>

		<ul class="mt-2 space-x-2">
			<li class="inline-block bg-base-1 px-2 py-0.5 rounded-md text-base-12 -mr-1">
				<span>
					{media.info.rating}
				</span>
			</li>

			{#each Object.entries(media.info.ratings) as [critic, rating]}
				<li class="inline-block bg-base-1 px-2 py-0.5 rounded-md text-base-12">
					<icon
						class="inline-block translate-y-0.5 h-4 w-fit"
						data-icon="critic/{critic.toLowerCase()}"
					/>
					<span>
						{rating}
					</span>
				</li>
			{/each}
		</ul>

		<div class="mt-8">
			<a
				href="/media/{media.id}/player?quality={selectedVideoQuality}&streams={selectedVideoStream.id},{selectedAudioStream.id}"
				class="bg-primary-8 px-2 py-1 rounded-md"
			>
				<icon class="inline-block translate-y-0.5 h-4 w-4" data-icon="solid/play" />
				Watch
			</a>
		</div>

		<p class="mt-6">
			{media.info.summary}
		</p>

		<div class="mt-6 flex space-x-6">
			<ul class="flex-0 w-50">
				<li>
					<span class="text-base-11">Directors:</span>
					{media.info.people.directors.join(', ')}
				</li>
				<li>
					<span class="text-base-11">Writers:</span>
					{media.info.people.writers.join(', ')}
				</li>
				<li>
					<span class="text-base-11">Starring:</span>
					{media.info.people.actors.join(', ')}
				</li>
			</ul>
			<ul class="flex-0 w-50">
				<li>
					<span class="text-base-11">Studios:</span>
					{media.info.studios.join(', ')}
				</li>
				<li>
					<span class="text-base-11">Video:</span>
					{media.files.streams.video.map((s) => s.name || s.codec).join(', ')}
				</li>
				<li>
					<span class="text-base-11">Audio:</span>
					{media.files.streams.audio.map((s) => s.name || s.codec).join(', ')}
				</li>
			</ul>
		</div>
	</div>
</div>
