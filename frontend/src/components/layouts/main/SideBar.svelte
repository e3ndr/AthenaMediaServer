<script>
	import { page } from '$app/stores';

	const SECTIONS = [
		[
			{ name: 'Home', icon: 'outline/home', href: '/', useStartsWith: false },
			{ name: 'Movies', icon: 'outline/film', href: '/media/movies', useStartsWith: true }
		],
		[
			{ name: 'Settings', icon: 'outline/cog', href: '/settings', useStartsWith: true },
			{
				name: 'GitHub',
				icon: 'outline/code-bracket',
				href: 'https://github.com/e3ndr/AthenaMediaServer',
				useStartsWith: true
			}
		]
	];
</script>

<div class="relative flex w-full max-w-xs flex-1 flex-col bg-base-1 pt-5 pb-4 overflow-hidden">
	<div class="flex flex-shrink-0 items-center px-4">
		<span class="ml-1 font-medium text-lg"> AthenaMediaServer </span>
	</div>

	<nav
		class="h-full flex-shrink-0 divide-y divide-current text-base-5 overflow-y-auto space-y-3 mt-1"
		aria-label="SideBar"
	>
		{#each SECTIONS as sectionItems}
			<div class="pt-3 space-y-1">
				{#each sectionItems as item}
					{@const isSelected = item.useStartsWith
						? $page.url.pathname.startsWith(item.href)
						: $page.url.pathname == item.href}

					<a
						href={item.href}
						target={item.href.startsWith('https://') ? '_blank' : undefined}
						class="transition mx-1 text-base-11 hover:text-base-12 hover:bg-base-7 group flex items-center px-2 py-2 text-base font-medium rounded-md"
						class:bg-base-6={isSelected}
						class:text-base-12={isSelected}
					>
						<icon class="h-6 mr-2" data-icon={item.icon} />

						{item.name}
					</a>
				{/each}
			</div>
		{/each}
	</nav>
</div>
