<script>
	import PageTitle from '$lib/PageTitle.svelte';
	export let data;
</script>

<svelte:head>
	{#each data.errors as error}
		<button
			class="error-alert"
			onclick="alert('An error occurred whilst loading library:\n\n' + {JSON.stringify(error)});"
			style="display: none;"
		/>
	{/each}
	<script>
		var errorAlerts = document.getElementsByClassName('error-alert');
		for (var i = 0; i < errorAlerts.length; i++) {
			errorAlerts[i].click();
		}
	</script>
</svelte:head>

<PageTitle title={['Media']} />

<a href="/settings" style="position: absolute; right: 6px; top: 4px;">Settings</a>

<div>
	{#each data.mediaList as media}
		<a
			style="margin: 10px; display: inline-block;"
			href="/media/{media.id}"
			title="{media.info.title} ({media.info.year})"
		>
			<img
				style="width: 200px; height: 320px; border-radius: 20px; object-fit: cover;"
				src={media.files.images.posterUrl}
				alt="{media.info.title} Poster"
			/>
			<h1 style="font-size: small; font-weight: 500;">
				{media.info.title}
			</h1>
		</a>
	{/each}
</div>
